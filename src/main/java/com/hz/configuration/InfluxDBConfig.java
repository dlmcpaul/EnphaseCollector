package com.hz.configuration;

import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class InfluxDBConfig {

	private final EnphaseCollectorProperties config;

	private static final String SOLAR_DATABASE_NAME = "solardb";
	private static final String SOLAR_METRICS_DATABASE_NAME = "collectorStats";
	private static final String SOLAR_METRICS_ORGANISATION = "hzindustries";

	@Bean
	@Profile({"influxdb"})
	public InfluxMeterRegistry influxMeterRegistry() {

		InfluxConfig metricsConfig;

		if (config.getInfluxdbResource().isTokenSet()) {
			// Configure for V2 InfluxDB
			metricsConfig = new InfluxConfig() {
				@NotNull
				@Override
				public String uri() {
					return config.getInfluxdbResource().getUrl();
				}

				@Override
				public String token() {
					return config.getInfluxdbResource().getToken();
				}

				@Override
				public @NotNull String bucket() {
					return SOLAR_METRICS_DATABASE_NAME;
				}

				@Override
				public String org () {
					return SOLAR_METRICS_ORGANISATION;
				}
				@Override
				public String get(@NotNull String k) {
					return null; // accept the rest of the defaults
				}
			};
		} else {
			// Configure for V1 InfluxDB
			metricsConfig = new InfluxConfig() {

				@NotNull
				@Contract(pure = true)
				@Override
				public String db() {
					return SOLAR_METRICS_DATABASE_NAME;
				}

				@NotNull
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
				public String get(@NotNull String k) {
					return null; // accept the rest of the defaults
				}
			};
		}

		log.info("Writing micrometer metrics to influx {} database at {}", metricsConfig.apiVersion().toString(), config.getInfluxdbResource().getUrl());

		return InfluxMeterRegistry.builder(metricsConfig).build();
	}

	@Bean
	@Profile({"influxdb"})
	public InfluxDB destinationInfluxDB() {
		log.info("Writing solar stats to influx database at {}", config.getInfluxdbResource().getUrl());

		InfluxDB database;

		try {
			if (config.getInfluxdbResource().noAuthenticationSet()) {
				log.info("Connecting to InfluxDB without credentials");
				database = InfluxDBFactory.connect(config.getInfluxdbResource().getUrl());
			} else if (config.getInfluxdbResource().isTokenSet()) {
				log.info("Connecting to InfluxDB with token");
				OkHttpClient.Builder client = new OkHttpClient.Builder();

				client.addInterceptor(chain -> {
					Request original = chain.request();

					Request request = original.newBuilder()
							.header("Authorization", "Token " + config.getInfluxdbResource().getToken())
							.method(original.method(), original.body())
							.build();

					return chain.proceed(request);
				});
				database = InfluxDBFactory.connect(config.getInfluxdbResource().getUrl(), client);
			} else {
				log.info("Connecting to InfluxDB with credentials as user {}", config.getInfluxdbResource().getUser());
				database = InfluxDBFactory.connect(config.getInfluxdbResource().getUrl(), config.getInfluxdbResource().getUser(), config.getInfluxdbResource().getPassword());
			}

			database.query(new Query(String.format("CREATE DATABASE \"%1s\" WITH DURATION 365d", SOLAR_DATABASE_NAME)));
		} catch (Exception e) {
			log.error("InfluxDB Exception: {}", e.getMessage());
			throw e;
		}

		database.setDatabase(SOLAR_DATABASE_NAME);
		database.enableBatch(BatchOptions.DEFAULTS);
		database.setLogLevel(InfluxDB.LogLevel.NONE);
		return database;
	}
}
