package com.hz.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Log4j2
@Profile({"!testing"})
public class EnphaseSystemInfoConfig {

	private final RestTemplate enphaseRestTemplate;

	@Bean
	public EnvoyInfo envoyInfo() {
		try {
			ObjectMapper xmlMapper = new XmlMapper();
			xmlMapper.registerModule(new JaxbAnnotationModule());
			String infoXml = enphaseRestTemplate.getForObject(EnphaseRestClientConfig.CONTROLLER, String.class);
			if (infoXml != null) {
				return (EnvoyInfo) xmlMapper.readValue(infoXml, EnvoyInfo.class);
			}
		} catch (IOException | ResourceAccessException e) {
			log.warn("Failed to read envoy info page.  Exception was {}", e.getMessage());
		}

		return new EnvoyInfo("Unknown", "Unknown");
	}
}
