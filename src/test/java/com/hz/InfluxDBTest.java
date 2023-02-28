package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.influxdb.InfluxDB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"testing", "influxdb"})
@Import(TestEnphaseSystemInfoConfig.class)
class InfluxDBTest {

	@Container
	private static GenericContainer influx = new GenericContainer(DockerImageName.parse("influxdb:1.8"))
			.withExposedPorts(8086);

	@DynamicPropertySource
	static void registerMySQLProperties(DynamicPropertyRegistry registry) {
		registry.add("envoy.influxdbResource.host", influx::getHost);
		registry.add("envoy.influxdbResource.port", influx::getFirstMappedPort);
	}

	@Test
	void influxDBStarted(@Autowired InfluxDB destinationInfluxDB) {
		assertThat(influx.isCreated());
		assertThat(destinationInfluxDB).isNotNull();
	}

	// Need to handle cleanup as Container will go away before spring teardown of beans
	@AfterAll
	static void shutdown(@Autowired InfluxMeterRegistry influxMeterRegistry) {
		influxMeterRegistry.close();
		influxMeterRegistry.clear();
	}

}
