package com.hz.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by David on 24-Oct-17.
 */
@Configuration
public class DestinationRestClientConfig {
	private static final Logger LOG = LoggerFactory.getLogger(DestinationRestClientConfig.class);

	private final EnphaseCollectorConfig config;

	@Autowired
	public DestinationRestClientConfig(EnphaseCollectorConfig config) {
		this.config = config;
	}

	@Bean
	public RestTemplate destinationRestTemplate(RestTemplateBuilder builder) {

		LOG.info("Writing to destination {}", config.getMetricDestination().getUrl());
		return builder
				.rootUri(config.getMetricDestination().getUrl())
				.setConnectTimeout(5000)
				.setReadTimeout(5000)
				.build();
	}
}
