package com.hz.configuration;

import com.hz.models.envoy.xml.EnvoyInfo;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Arrays;

@TestConfiguration
@Profile("testing")
public class TestEnphaseSystemInfoConfig {
	@Bean
	public EnvoyInfo envoyInfo() {
		return new EnvoyInfo("unknown","unknown");
	}

	@Bean
	@Primary
	public HttpMessageConverters messageConverters() {
		return new HttpMessageConverters(false, Arrays.asList(new MappingJackson2HttpMessageConverter()));
	}

}
