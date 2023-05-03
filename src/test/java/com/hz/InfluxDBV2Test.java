package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.InfluxDBContainer;
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
class InfluxDBV2Test {

	@Container
	private static final InfluxDBContainer<?> influx = new InfluxDBContainer<>(DockerImageName.parse("influxdb:2.0.7"))
			.withBucket("collectorStats")
			.withOrganization("hzindustries")
			.withAdminToken("token");

	@Autowired
	InfluxDB destinationInfluxDB;

	@Autowired
	InfluxMeterRegistry influxMeterRegistry;

	@DynamicPropertySource
	static void registerInfluxProperties(DynamicPropertyRegistry registry) {
		registry.add("envoy.influxdbResource.host", influx::getHost);
		registry.add("envoy.influxdbResource.port", influx::getFirstMappedPort);
		registry.add("envoy.influxdbResource.token", () -> influx.getAdminToken().orElse(""));
	}

	@Test
	@Order(1)
	void influxDBStarted() {
		assertThat(influx.isCreated()).isTrue();
		assertThat(destinationInfluxDB).isNotNull();
		assertThat(destinationInfluxDB.ping().isGood()).isTrue();
	}

	@Test
	@Order(2)
	void writeMetric() {
		assertDoesNotThrow(() -> {
			destinationInfluxDB.write(Point.measurement("test-measurement").time(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), TimeUnit.MILLISECONDS).addField("value", 77f).build());
			destinationInfluxDB.flush();
		}, "Failed to write metric to influxdb");
	}

	@Test
	@Order(3)
	void flushMicrometerMetrics() {
		assertDoesNotThrow(() -> {
			influxMeterRegistry.close();
			influxMeterRegistry.clear();
		}, "Failed to flush micrometer metrics");
	}

}
