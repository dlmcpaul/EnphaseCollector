package com.hz.configuration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional Value depending on the activation of the h2 console
 * h2 console needs unsafe-inline for scripts and css
 * but for normal usage we want to restrict to self only
 */

@Configuration
@Log4j2
public class ContentSecurityPolicyConfig {

	@Bean(name = "contentSecurityPolicyValue")
	@ConditionalOnProperty(
			value="spring.h2.console.enabled",
			havingValue = "true")
	public String h2ConsoleEnabled() {
		return "default-src 'self' data:; img-src 'self'; script-src 'self'; style-src 'self'; script-src-elem 'self' 'unsafe-inline'; style-src-attr 'self' 'unsafe-inline';";
	}

	@Bean(name = "contentSecurityPolicyValue")
	@ConditionalOnProperty(
			value="spring.h2.console.enabled",
			havingValue = "false",
			matchIfMissing = true)
	public String h2ConsoleDisabled() {
		return "default-src 'self' data:; img-src 'self'; script-src 'self'; style-src 'self';";
	}

}
