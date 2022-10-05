package com.hz.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("envoy")
@Data
public class SecretConfig {
	// V7 Auto fetch configuration
	private String enphaseWebUser;
	private String enphaseWebPassword;
	private String envoySerialNumber;
	private String installerPassword;
}
