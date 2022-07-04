package com.hz.configuration;

import com.hz.components.EnphaseRequestRetryHandler;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static com.hz.configuration.EnphaseURLS.INVERTERS;
import static com.hz.configuration.EnphaseURLS.SYSTEM;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class EnphaseV7RestClientConfig {

	private final EnphaseCollectorProperties config;
	private final EnvoyInfo envoyInfo;

	private RestTemplate createTemplate(RestTemplateBuilder builder) {
		Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, config.getBearerToken());

		HttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
				.setDefaultHeaders(List.of(header))
				.build();

		return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)))
				.build();
	}

	@Bean
	@ConditionalOnProperty(name="envoy.bearer.token")
	public RestTemplate enphaseRestTemplate(RestTemplateBuilder builder) {
		log.info("Reading from insecure V7 Envoy controller endpoint {}{}", config.getController().getUrl(), SYSTEM);

		return createTemplate(builder);
	}

	@Bean
	@ConditionalOnProperty(name="envoy.bearer.token")
	public RestTemplate enphaseSecureRestTemplate(RestTemplateBuilder builder) {
		log.info("Reading from protected V7 Envoy controller endpoint {}{}", config.getController().getUrl(), INVERTERS);

		return createTemplate(builder);
	}

}
