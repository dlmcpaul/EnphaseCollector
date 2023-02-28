package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"testing", "influxdb"})
@Import(TestEnphaseSystemInfoConfig.class)
@Log4j2
class InfluxDBTest {

	@Container
	private static final GenericContainer<?> influx = new GenericContainer<>(DockerImageName.parse("influxdb:1.8"))
			.withExposedPorts(8086)
			.withEnv("INFLUXDB_HTTP_AUTH_ENABLED", "false")
			.waitingFor(new WaitAllStrategy());

	@Autowired
	InfluxDB destinationInfluxDB;

	@DynamicPropertySource
	static void registerMySQLProperties(DynamicPropertyRegistry registry) {
		registry.add("envoy.influxdbResource.host", influx::getHost);
		registry.add("envoy.influxdbResource.port", influx::getFirstMappedPort);
	}

	@Test
	void influxDBStarted() {
		assertThat(influx.isCreated()).isTrue();
		assertThat(destinationInfluxDB).isNotNull();
	}

	@Test
	void writeMetric() {
		assertDoesNotThrow(() -> {
			destinationInfluxDB.write(Point.measurement("testmeasurment").time(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), TimeUnit.MILLISECONDS).addField("value", 77f).build());
			destinationInfluxDB.flush();
		}, "Failed to write metric to influxdb");
	}

	// Need to handle cleanup as Container will go away before spring teardown of beans
	@AfterAll
	static void shutdown(@Autowired InfluxMeterRegistry influxMeterRegistry) {
		influxMeterRegistry.close();
		influxMeterRegistry.clear();
	}

}
