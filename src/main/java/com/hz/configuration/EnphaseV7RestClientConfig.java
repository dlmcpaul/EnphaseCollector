package com.hz.configuration;

import com.hz.components.EnphaseRequestRetryHandler;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;

import static com.hz.configuration.EnphaseURLS.*;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class EnphaseV7RestClientConfig {

	private final EnphaseCollectorProperties config;
	private final EnvoyInfo envoyInfo;

	private RestTemplate createTemplate(RestTemplateBuilder builder) {
		Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getBearerToken());

		BasicCookieStore cookieStore = new BasicCookieStore();

		try {
			HttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
				.setDefaultHeaders(List.of(header))
				.setSSLContext(new SSLContextBuilder().loadTrustMaterial(new TrustAllStrategy()).build())
				.setSSLHostnameVerifier(new NoopHostnameVerifier())
				.setDefaultCookieStore(cookieStore)
				.build();

				// Make a call to the /auth/check_jwt endpoint to set the cookie
				HttpResponse response = httpClient.execute(new HttpGet(config.getController().getUrl() + AUTH_CHECK));
				if (response.getStatusLine().getStatusCode() != 200) {
					log.error("Attempt to validate bearer token {} against {} failed with result {}", config.getBearerToken(), config.getController().getUrl() + AUTH_CHECK, response.getStatusLine());
				}
				log.info("Cookie Store now has {} cookies after result {}", cookieStore.getCookies().size(), response.getStatusLine());

			return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)))
				.build();

		} catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			log.error("Could not connect to envoy when configuring a v7 http client - {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Bean
	@ConditionalOnProperty(name="envoy.bearer-token")
	public RestTemplate enphaseRestTemplate(RestTemplateBuilder builder) {
		log.info("Running against Envoy Software {}", envoyInfo.getSoftwareVersion());

		log.info("Configuring insecure RestTemplate for V7 Envoy controller endpoint {}{}", config.getController().getUrl(), SYSTEM);

		return createTemplate(builder);
	}

	@Bean
	@ConditionalOnProperty(name="envoy.bearer-token")
	public RestTemplate enphaseSecureRestTemplate(RestTemplateBuilder builder) {
		log.info("Configuring protected RestTemplate for V7 Envoy controller endpoint {}{}", config.getController().getUrl(), INVERTERS);

		return createTemplate(builder);
	}

}
