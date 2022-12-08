package com.hz.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.hz.components.EnphaseRequestRetryHandler;
import com.hz.models.envoy.AuthorisationToken;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Log4j2
@Profile({"!testing"})
public class EnphaseSystemInfoConfig {

	private final EnphaseCollectorProperties config;

	private RestTemplate infoRestTemplate(RestTemplateBuilder builder) {

		HttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
				.build();

		return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)))
				.build();
	}

	@Bean
	public EnvoyInfo envoyInfo(RestTemplateBuilder restTemplateBuilder) {
		log.info("Reading system information from Envoy controller endpoint {}{}", config.getController().getUrl(), EnphaseURLS.CONTROLLER);
		try {
			ObjectMapper xmlMapper = new XmlMapper();
			xmlMapper.registerModule(new JaxbAnnotationModule());
			xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			String infoXml = infoRestTemplate(restTemplateBuilder).getForObject(EnphaseURLS.CONTROLLER, String.class);
			if (infoXml != null) {
				return xmlMapper.readValue(infoXml, EnvoyInfo.class);
			}
		} catch (IOException | ResourceAccessException e) {
			log.warn("Failed to read envoy info page.  Exception was {}", e.getMessage());
		}

		return new EnvoyInfo("Unknown", "Unknown");
	}

	@Bean
	public AuthorisationToken getAuthorisation(EnvoyInfo envoyInfo) throws JsonProcessingException {
		if (envoyInfo.isV7orAbove()) {
			if (config.getBearerToken() == null || config.getBearerToken().trim().isEmpty()) {

				if (config.getEnphaseWebUser() != null && config.getEnphaseWebUser().trim().isEmpty()
						&& config.getEnphaseWebPassword() != null && config.getEnphaseWebPassword().trim().isEmpty()
						&& envoyInfo.getSerialNumber() != null && envoyInfo.getSerialNumber().trim().isEmpty()) {
					log.error("Neither Bearer Token or Enphase Web User details provided.  Cannot generate authentication");
				}
				return AuthorisationToken.makeV7TokenFetched(config.getEnphaseWebUser(), config.getEnphaseWebPassword(), envoyInfo.getSerialNumber());
			}

			return AuthorisationToken.makeV7TokenProvided(config.getBearerToken());
		}

		return AuthorisationToken.makeV5(envoyInfo, config.getController().getPassword());
	}

	/**
	 * Needed for /ivp/meters and /ivp/meters/readings
	 * @return customer converter to handle json as application octet stream
	 */
	@Bean
	public HttpMessageConverters customConverters() {
		MappingJackson2HttpMessageConverter octetStreamConverter = new MappingJackson2HttpMessageConverter();
		octetStreamConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
		return new HttpMessageConverters(octetStreamConverter);
	}

}
