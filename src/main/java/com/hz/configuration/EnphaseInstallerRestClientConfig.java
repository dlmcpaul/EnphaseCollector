package com.hz.configuration;

import com.hz.components.EnphaseRequestRetryHandler;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.utils.InstallerPasswordCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
public class EnphaseInstallerRestClientConfig {

	// needs installer user and password
	public static final String METER_STREAM = "/stream/meter";

    private final EnphaseCollectorProperties config;
    private final EnvoyInfo envoyInfo;

	private String getInstallerPassword() {
		return InstallerPasswordCalculator.getPassword(envoyInfo.getSerialNumber());
	}

	private CredentialsProvider provider() {
		if (envoyInfo.isV7orAbove()) {
			log.info("No Security Provider available for Envoy with this software version {}", envoyInfo.getSoftwareVersion());
		} else {
			log.info("Preparing Installer Realm Authentication Provider with user {}", InstallerPasswordCalculator.USERNAME);
		}

		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
				new UsernamePasswordCredentials(InstallerPasswordCalculator.USERNAME, getInstallerPassword());
		provider.setCredentials(new AuthScope(config.getController().getHost(), 80, InstallerPasswordCalculator.REALM, ANY_SCHEME), credentials);
		return provider;
	}

    @Bean
    @ConditionalOnProperty(name="envoy.bearer-token", havingValue="false", matchIfMissing = true)
    public RestTemplate enphaseInstallerRestTemplate(RestTemplateBuilder builder) {

	    log.info("Reading from installer Envoy controller endpoint {}{}", config.getController().getUrl(), METER_STREAM);

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
