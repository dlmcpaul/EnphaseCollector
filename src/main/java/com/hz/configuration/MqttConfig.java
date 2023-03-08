package com.hz.configuration;

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
		if (config.getMqqtResource() == null) {
			throw new RuntimeException("Please configure the mqtt settings");
		}

		EnphaseCollectorProperties.MqqtResource mqqtResource = config.getMqqtResource();

		MqttConnectOptions options = new MqttConnectOptions();
		if (mqqtResource.isUserEmpty() == false) {
			options.setUserName(mqqtResource.getUser());
		}
		if (mqqtResource.isPasswordEmpty() == false) {
			options.setPassword(mqqtResource.getPassword().toCharArray());
		}
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);

		String publisherId = mqqtResource.isPublisherIdEmpty() ? DEFAULTPUBLISHERID : mqqtResource.getPublisherId();

		IMqttClient publisher = new MqttClient(mqqtResource.getUrl(), publisherId);
		publisher.connect(options);
		return publisher;
	}
}
