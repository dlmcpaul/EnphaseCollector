package com.hz.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("secret")
@Component
@Data
public class SecretConfig {
	// V7 Auto fetch configuration
	private String enphaseWebUser;
	private String enphaseWebPassword;
	private String envoySerialNumber;
	private String installerPassword;
}
