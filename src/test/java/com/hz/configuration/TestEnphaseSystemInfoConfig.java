package com.hz.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import com.hz.models.envoy.AuthorisationToken;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.util.List;

@TestConfiguration
@Profile("testing")
@Log4j2
@RequiredArgsConstructor
public class TestEnphaseSystemInfoConfig {

	private final EnphaseCollectorProperties config;

	@Bean
	public String mockEnvoyInfo() {
		return "<?xml version='1.0' encoding='UTF-8'?><envoy_info><device><sn>Unknown</sn><software>Unknown</software></device></envoy_info>";
	}

	@Bean
	public EnvoyInfo envoyInfo(String mockEnvoyInfo) {
		log.info("Creating Mocked EnvoyInfo");
		try {
			ObjectMapper xmlMapper = new XmlMapper();
			xmlMapper.registerModule(new JakartaXmlBindAnnotationModule());
			xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);   // We want to fail on unknown properties, so we can test new releases

			return xmlMapper.readValue(mockEnvoyInfo, EnvoyInfo.class);
		} catch (IOException e) {
			return new EnvoyInfo(e.getMessage(),e.getMessage());
		}
	}

	@Bean
	public AuthorisationToken getAuthorisation(EnvoyInfo envoyInfo) throws JsonProcessingException {
		if (envoyInfo.isV7orAbove()) {
			if (config.getBearerToken() == null || config.getBearerToken().isEmpty()) {
				return AuthorisationToken.makeV7TokenFetched(config.getEnphaseWebUser(), config.getEnphaseWebPassword(), envoyInfo.getSerialNumber());
			}

			return AuthorisationToken.makeV7TokenProvided(config.getBearerToken());
		}

		return AuthorisationToken.makeV5(envoyInfo, config.getController().getPassword());
	}

	@Bean
	@Primary
	public HttpMessageConverters messageConverters() {
		return new HttpMessageConverters(false, List.of(new MappingJackson2HttpMessageConverter()));
	}

}
