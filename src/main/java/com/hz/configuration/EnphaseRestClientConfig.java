package com.hz.configuration;

import com.hz.components.EnphaseRequestRetryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

import static com.hz.configuration.EnphaseURLS.SYSTEM;

/**
 * Created by David on 22-Oct-17.
 */
@Configuration
@RequiredArgsConstructor
@Log4j2
public class EnphaseRestClientConfig {

    private final EnphaseCollectorProperties config;

	@Bean
	@ConditionalOnProperty(name="envoy.bearer-token", havingValue="false", matchIfMissing = true)
	public RestTemplate enphaseRestTemplate(RestTemplateBuilder builder) {
		log.info("Reading from insecure Envoy controller endpoint {}{}", config.getController().getUrl(), SYSTEM);

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
