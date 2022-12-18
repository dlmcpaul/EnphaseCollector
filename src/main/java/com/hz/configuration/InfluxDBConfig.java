package com.hz.configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class InfluxDBConfig {

	private final EnphaseCollectorProperties config;

	private static final String DATABASE_NAME = "solardb";

	@Bean
	@Profile({"influxdb"})
	public InfluxMeterRegistry influxMeterRegistry() {
		log.info("Writing metrics to influx database at {}", config.getInfluxdbResource().getUrl());

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
			public String userName() {
				return config.getInfluxdbResource().getUser();
			}

			@Override
			public String password() {
				return config.getInfluxdbResource().getPassword();
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
		log.info("Writing solar stats to influx database at {}", config.getInfluxdbResource().getUrl());

		InfluxDB database = null;

		if (config.getInfluxdbResource().getUser() == null || config.getInfluxdbResource().getUser().isEmpty()) {
			InfluxDBFactory.connect(config.getInfluxdbResource().getUrl());
		} else {
			InfluxDBFactory.connect(config.getInfluxdbResource().getUrl(), config.getInfluxdbResource().getUser(), config.getInfluxdbResource().getPassword());
		}
		database.query(new Query("CREATE DATABASE \"" + DATABASE_NAME + "\" WITH DURATION 365d", DATABASE_NAME));

		database.setDatabase(DATABASE_NAME);
		database.enableBatch(BatchOptions.DEFAULTS);
		database.setLogLevel(InfluxDB.LogLevel.NONE);
		return database;
	}
}
