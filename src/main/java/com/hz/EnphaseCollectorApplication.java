package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.export.influx.InfluxMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.influx.InfluxDbAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

// We only want InfluxDB configurations if profile set to influxdb so exclude from autoconfig
@SpringBootApplication(exclude = {InfluxMetricsExportAutoConfiguration.class, InfluxDbAutoConfiguration.class})
@EnableConfigurationProperties(EnphaseCollectorProperties.class)
@EnableScheduling
public class EnphaseCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnphaseCollectorApplication.class, args);
	}
}
