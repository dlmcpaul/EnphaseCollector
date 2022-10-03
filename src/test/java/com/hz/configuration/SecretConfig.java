package com.hz.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("envoy")
@Data
public class SecretConfig {
	// V7 Autofetch configuration
	private String enphaseWebUser;
	private String enphaseWebPassword;
	private String envoySerialNumber;
	private String installerPassword;
}
