package com.hz.configuration;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.apache.http.auth.AuthScope.ANY_SCHEME;

/**
 * Created by David on 22-Oct-17.
 */
@Configuration
public class EnphaseRestClientConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EnphaseRestClientConfig.class);

    public static final String SYSTEM = "/home.json";
    public static final String INVENTORY = "/inventory.json?deleted=1";
    public static final String PRODUCTION = "/production.json?details=1";
    public static final String CONTROLLER = "/info.xml";
    public static final String WIFI_INFO = "/admin/lib/wireless_display.json?site_info=0";
	public static final String WAN_INFO = "/admin/lib/network_display.json";

    // Needs Digest authentication
    public static final String INVERTERS = "/api/v1/production/inverters";
	private static final String REALM = "enphaseenergy.com";

    private final EnphaseCollectorProperties config;

	@Autowired
	public EnphaseRestClientConfig(EnphaseCollectorProperties config) {
		this.config = config;
	}

	private CredentialsProvider provider() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
				new UsernamePasswordCredentials(config.getController().getUser(), config.getController().getPassword());
		provider.setCredentials(new AuthScope(config.getController().getHost(), 80, REALM, ANY_SCHEME), credentials);
		return provider;
	}

	@Bean
	public RestTemplate enphaseRestTemplate(RestTemplateBuilder builder) {

		LOG.info("Reading from insecure Envoy controller endpoint {}", config.getController().getUrl());

		return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.build();
	}

    @Bean
    public RestTemplate enphaseSecureRestTemplate(RestTemplateBuilder builder) {

	    LOG.info("Reading from protected Envoy controller endpoint {}", config.getController().getUrl());

	    HttpClient httpClient = HttpClients
			    .custom()
			    .setDefaultCredentialsProvider(provider())
			    .useSystemProperties()
			    .build();

	    return builder
			    .rootUri(config.getController().getUrl())
			    .setConnectTimeout(Duration.ofSeconds(5))
			    .setReadTimeout(Duration.ofSeconds(30))
			    .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
			    .build();
    }

}
