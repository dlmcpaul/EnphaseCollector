package com.hz.configuration;

import com.hz.components.EnphaseRequestRetryHandler;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.apache.http.auth.AuthScope.ANY_SCHEME;

/**
 * Created by David on 22-Oct-17.
 */
@Configuration
@RequiredArgsConstructor
@Log4j2
public class EnphaseSecureRestClientConfig {

    // Needs Digest authentication
    public static final String INVERTERS = "/api/v1/production/inverters";
	private static final String REALM = "enphaseenergy.com";

    private final EnphaseCollectorProperties config;
    private final EnvoyInfo envoyInfo;

	private String getControllerPassword() {
		if (config.getController().getPassword() == null || config.getController().getPassword().isEmpty()) {
			return envoyInfo.getSerialNumber().substring(envoyInfo.getSerialNumber().length()-6);
		}

		return config.getController().getPassword();
	}

	private CredentialsProvider provider() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
				new UsernamePasswordCredentials(config.getController().getUser(), getControllerPassword());
		provider.setCredentials(new AuthScope(config.getController().getHost(), 80, REALM, ANY_SCHEME), credentials);
		return provider;
	}

    @Bean
    public RestTemplate enphaseSecureRestTemplate(RestTemplateBuilder builder) {

	    log.info("Reading from protected Envoy controller endpoint {}", config.getController().getUrl());

	    HttpClient httpClient = HttpClients
			    .custom()
			    .setDefaultCredentialsProvider(provider())
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

}
