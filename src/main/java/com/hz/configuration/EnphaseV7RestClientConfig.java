package com.hz.configuration;

import com.hz.components.EnphaseRequestRetryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static com.hz.configuration.EnphaseURLS.*;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class EnphaseV7RestClientConfig {

	private final EnphaseCollectorProperties config;

	private RestTemplate createTemplate(RestTemplateBuilder builder) {
		Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, config.getBearerToken());

		BasicCookieStore cookieStore = new BasicCookieStore();

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
				.setDefaultHeaders(List.of(header))
				.setSSLHostnameVerifier(new NoopHostnameVerifier())
				.setDefaultCookieStore(cookieStore)
				.build();

		// Make a call to the /auth/check_jwt endpoint to set the cookie
		try {
			log.info("Attempting to validate bearer token {} with local envoy", config.getBearerToken());
			CloseableHttpResponse response = httpClient.execute(new HttpGet(config.getController().getUrl() + AUTH_CHECK));
			if (response.getStatusLine().getStatusCode() != 200) {
				log.error("Attempt to validate V7 bearer token failed - {}", response.getStatusLine());
			}
			log.info("Cookie Store now has {} cookies", cookieStore.getCookies().size());
		} catch (IOException e) {
			log.error("Could not connect to envoy when configuring http client - {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}

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
