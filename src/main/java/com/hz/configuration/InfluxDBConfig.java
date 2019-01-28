package com.hz.configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
public class InfluxDBConfig {
	private static final Logger LOG = LoggerFactory.getLogger(InfluxDBConfig.class);

	private final EnphaseCollectorProperties config;

	private static String DATABASE_NAME = "solardb";

	@Autowired
	public InfluxDBConfig(EnphaseCollectorProperties config) {
		this.config = config;
	}

	@Bean
	@Profile({"influxdb"})
	public InfluxDB destinationInfluxDB() {
		LOG.info("Writing to influx database at {}", config.getInfluxdbResource().getUrl());

		//influx stats
		//management.metrics.export.influx.auto-create-db=true
		//management.metrics.export.influx.db=collectorStats
		//management.metrics.export.influx.retention-duration=24h
		//management.metrics.export.influx.uri=http://${envoy.influxdbResource.host}:${envoy.influxdbResource.port}

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
		MeterRegistry registry = new InfluxMeterRegistry(metricsConfig, Clock.SYSTEM);

		InfluxDB result = InfluxDBFactory.connect(config.getInfluxdbResource().getUrl());
		if (result.databaseExists(DATABASE_NAME) == false) {
			result.createDatabase(DATABASE_NAME);
			result.createRetentionPolicy("defaultPolicy", DATABASE_NAME, "365d", 1, true);
		}
		result.setDatabase(DATABASE_NAME);
		result.enableBatch(BatchOptions.DEFAULTS);
		result.setLogLevel(InfluxDB.LogLevel.NONE);
		return result;
	}
}
