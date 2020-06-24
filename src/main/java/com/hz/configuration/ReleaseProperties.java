package com.hz.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:/release.properties", ignoreResourceNotFound = true)
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("release")
public class ReleaseProperties {
	private String version;
}
