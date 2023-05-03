package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"testing", "mqtt"})
@Import(TestEnphaseSystemInfoConfig.class)
@Log4j2
class MqqtTest {

	@Container
	private static final HiveMQContainer hivemqCe = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce").withTag("2021.3"))
			.withLogLevel(Level.DEBUG);

	@Autowired
	IMqttClient mqttClient;

	@DynamicPropertySource
	static void registerHiveMqProperties(DynamicPropertyRegistry registry) {
		registry.add("envoy.mqqtResource.host", hivemqCe::getHost);
		registry.add("envoy.mqqtResource.port", hivemqCe::getFirstMappedPort);
		registry.add("envoy.mqqtResource.topic", () -> "my-topic");
	}

	@Test
	void mqttStarted() {
		assertThat(hivemqCe.isCreated()).isTrue();
		assertThat(mqttClient).isNotNull();
	}

	@Test
	void writeMetric() {
		assertDoesNotThrow(() -> {
			String payload = "Message";

			MqttMessage msg = new MqttMessage(payload.getBytes());
			msg.setQos(0);
			msg.setRetained(true);
			mqttClient.publish("my-topic", msg);
		}, "Failed to write metric to mqtt");
	}
}
