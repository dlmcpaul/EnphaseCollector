package com.hz.configuration;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!standalone")
public class InfluxDBConfig {
	private static final Logger LOG = LoggerFactory.getLogger(InfluxDBConfig.class);

	private final EnphaseCollectorConfig config;

	@Autowired
	public InfluxDBConfig(EnphaseCollectorConfig config) {
		this.config = config;
	}

	@Bean
	public InfluxDB destinationInfluxDB() {
		LOG.info("Writing to influx database at {}", config.getMetricDestination().getUrl());

		InfluxDB result = InfluxDBFactory.connect(config.getMetricDestination().getUrl());
		result.setDatabase("solardb");
		result.enableBatch(BatchOptions.DEFAULTS);
		result.setLogLevel(InfluxDB.LogLevel.NONE);
		return result;
	}

}
