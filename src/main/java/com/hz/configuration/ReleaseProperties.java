package com.hz.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:/release.properties", ignoreResourceNotFound = true)
@Configuration
@ConfigurationProperties("release")
@Data
public class ReleaseProperties {
	private String version;
}
