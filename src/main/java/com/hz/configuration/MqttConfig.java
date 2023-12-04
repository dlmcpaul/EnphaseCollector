package com.hz.configuration;

import com.hz.exceptions.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class MqttConfig {

	private final EnphaseCollectorProperties config;

	private static final String DEFAULTPUBLISHERID = UUID.randomUUID().toString();

	@Profile("mqtt")
	@Bean
	public IMqttClient mqttClient() throws MqttException {
		EnphaseCollectorProperties.MqttResource mqttResource = config.getMqttResource();

		if (mqttResource == null) {
			throw new ConfigurationException("Please configure the mqtt settings");
		}

		String publisherId = mqttResource.isPublisherIdEmpty() ? DEFAULTPUBLISHERID : mqttResource.getPublisherId();

		log.info("Configuring MQTT Resource with Host {} Port {} publisher {} topic {}", mqttResource.getHost(), mqttResource.getPort(), publisherId, mqttResource.getTopic());

		MqttConnectOptions options = new MqttConnectOptions();
		if (mqttResource.isUserSet()) {
			options.setUserName(mqttResource.getUser());
		}
		if (mqttResource.isPasswordSet()) {
			options.setPassword(mqttResource.getPassword().toCharArray());
		}
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);

		IMqttClient publisher = new MqttClient(mqttResource.getUrl(), publisherId);
		publisher.connect(options);
		return publisher;
	}
}
