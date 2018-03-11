package com.hz;

import com.hz.configuration.EnphaseCollectorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(EnphaseCollectorConfig.class)
@EnableScheduling
public class EnphaseCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnphaseCollectorApplication.class, args);
	}
}
