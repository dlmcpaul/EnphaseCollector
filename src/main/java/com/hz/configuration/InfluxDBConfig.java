package com.hz.configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Profile;

@Configuration
public class InfluxDBConfig {
	private static final Logger LOG = LoggerFactory.getLogger(InfluxDBConfig.class);

	private final EnphaseCollectorProperties config;

	private static final String DATABASE_NAME = "solardb";

	@Autowired
	public InfluxDBConfig(EnphaseCollectorProperties config) {
		this.config = config;
	}

	@Bean
	@Profile({"influxdb"})
	public InfluxMeterRegistry influxMeterRegistry() {
		LOG.info("Writing metrics to influx database at {}", config.getInfluxdbResource().getUrl());

		InfluxConfig metricsConfig = new InfluxConfig() {

			@Override
			public String db() {
				return "collectorStats";
			}

			@Override
			public String uri() {
				return config.getInfluxdbResource().getUrl();
			}

			@Override
			public boolean autoCreateDb() {
				return true;
			}

			@Override
			public String retentionDuration() {
				return "24h";
			}

			@Override
			public String get(String k) {
				return null; // accept the rest of the defaults
			}
		};

		return new InfluxMeterRegistry(metricsConfig, Clock.SYSTEM);
	}

	@Bean
	@Profile({"influxdb"})
	public InfluxDB destinationInfluxDB() {
		LOG.info("Writing solar stats to influx database at {}", config.getInfluxdbResource().getUrl());

		InfluxDB database = InfluxDBFactory.connect(config.getInfluxdbResource().getUrl());
		database.query(new Query("CREATE DATABASE \"" + DATABASE_NAME + "\" WITH DURATION 365d", DATABASE_NAME));

		database.setDatabase(DATABASE_NAME);
		database.enableBatch(BatchOptions.DEFAULTS);
		database.setLogLevel(InfluxDB.LogLevel.NONE);
		return database;
	}
}
