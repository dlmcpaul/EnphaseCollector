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
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	public String baseUrl() {
		return config.getController().getUrl();
	}

	@Bean
	public HttpClientConnectionManager sslConnectionManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContext sslContext = SSLContexts.custom()
				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
				.build();
		Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory> create()
						.register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
						.register("http", new PlainConnectionSocketFactory())
						.build();

		return new PoolingHttpClientConnectionManager(socketFactoryRegistry);
	}

	@Bean
	public HttpClient createDefaultHttpClient(HttpClientConnectionManager sslConnectionManager) {
		return HttpClients
				.custom()
				.useSystemProperties()
				.setConnectionManager(sslConnectionManager)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.of(15, TimeUnit.SECONDS)))
				.build();
	}

	@Bean
	public RestClient defaultRestClient(HttpClientConnectionManager sslConnectionManager, @Qualifier("baseUrl") String baseUrl) {
		HttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setConnectionManager(sslConnectionManager)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.of(15, TimeUnit.SECONDS)))
				.build();

		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		httpRequestFactory.setConnectTimeout(Duration.ofSeconds(5));
		httpRequestFactory.setConnectionRequestTimeout(Duration.ofSeconds(15));

		return RestClient
				.builder()
				.baseUrl(baseUrl)
				.requestFactory(new BufferingClientHttpRequestFactory(httpRequestFactory))
				.build();
	}

	@Bean
	public RestTemplate enphaseSecureRestTemplate(RestTemplateBuilder restTemplateBuilder, HttpClient httpClient, @Qualifier("baseUrl") String baseUrl) {
		RestTemplate result = restTemplateBuilder
				.rootUri(baseUrl)
				.setConnectTimeout(Duration.ofSeconds(5))
				.requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)))
				.build();
		result.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));
		return result;
	}

	@Bean
	public EnvoyInfo envoyInfo(@Qualifier("mockEnvoyInfo") String mockEnvoyInfo) {
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
