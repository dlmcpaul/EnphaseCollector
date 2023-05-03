package com.hz.services;

import com.hz.components.EnphaseRequestRetryStrategy;
import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.exceptions.ConnectionException;
import com.hz.models.envoy.AuthorisationToken;
import com.hz.utils.EnphaseJWTExtractor;
import com.hz.utils.InstallerPasswordCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.hz.configuration.EnphaseURLS.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class EnvoyConnectionProxy {
	private static final String REALM = "enphaseenergy.com";

	private final AuthorisationToken authorisationToken;
	private final EnphaseCollectorProperties config;
	private final RestTemplateBuilder builder;

	private RestTemplate secureTemplate;
	private RestTemplate defaultTemplate;
	private RestTemplate installerTemplate;
	private final HttpClientConnectionManager sslConnectionManager;

	private RestTemplate buildTemplate(HttpClient httpClient) {
		return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)))
				.build();
	}

	private RestTemplate createDefaultRestTemplate() {
		log.info("Reading from insecure Envoy controller endpoint {}{}", config.getController().getUrl(), SYSTEM);

		HttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setRetryStrategy(new EnphaseRequestRetryStrategy())
				.build();

		return buildTemplate(httpClient);
	}

	private CredentialsProvider standardProvider() throws URISyntaxException {
		log.info("Preparing Realm Authentication Provider with user {}", config.getController().getUser());

		BasicCredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
				new UsernamePasswordCredentials(authorisationToken.getUser(), authorisationToken.getPassword().toCharArray());
		provider.setCredentials(new AuthScope(HttpHost.create(new URI(config.getController().getUrl())), REALM, null), credentials);
		return provider;
	}

	private CredentialsProvider installerProvider() throws URISyntaxException {
		log.info("Preparing Installer Realm Authentication Provider with user {}", InstallerPasswordCalculator.USERNAME);

		BasicCredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
			new UsernamePasswordCredentials(InstallerPasswordCalculator.USERNAME, InstallerPasswordCalculator.getPassword(authorisationToken.getSerialNo()).toCharArray());
		provider.setCredentials(new AuthScope(HttpHost.create(new URI(config.getController().getUrl())), REALM, null), credentials);
		return provider;
	}

	private RestTemplate createSecureRestTemplateV5(CredentialsProvider provider) {
		log.info("Reading from protected Envoy controller endpoint {}{}", config.getController().getUrl(), INVERTERS);

		HttpClient httpClient = HttpClients
				.custom()
				.setDefaultCredentialsProvider(provider)
				.useSystemProperties()
				.setRetryStrategy(new EnphaseRequestRetryStrategy())
				.build();

		return buildTemplate(httpClient);
	}

	private RestTemplate createSecureRestTemplateV7() {
		Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authorisationToken.getJwt());

		BasicCookieStore cookieStore = new BasicCookieStore();

		try {
			// Not good to ignore all the SSL checks
			HttpClient httpClient = HttpClients
					.custom()
					.useSystemProperties()
					.setRetryStrategy(new EnphaseRequestRetryStrategy())
					.setDefaultHeaders(List.of(header))
					.setConnectionManager(sslConnectionManager)
					.setDefaultCookieStore(cookieStore)
					.build();

			// Make a call to the /auth/check_jwt endpoint to set the cookie
			HttpResponse response = httpClient.execute(new HttpGet(config.getController().getUrl() + AUTH_CHECK));
			if (response.getCode() != 200) {
				log.error("Attempt to validate bearer token {} against {} failed with result {}", authorisationToken.getJwt(), config.getController().getUrl() + AUTH_CHECK, response.getCode());
			}

			return buildTemplate(httpClient);

		} catch (IOException e) {
			log.error("Could not connect to envoy when configuring a v7 http client - {}", e.getMessage(), e);
			throw new ConnectionException(e);
		}

	}

	public RestTemplate getSecureTemplate() throws IOException, URISyntaxException {
		if (secureTemplate == null) {
			if (authorisationToken.isV5()) {
				log.debug("Creating a new secure V5 access template");
				secureTemplate = createSecureRestTemplateV5(standardProvider());
			} else if (authorisationToken.canFetchToken()) {
				log.debug("Creating a new secure V7 access template after fetching token from Enphase");
				authorisationToken.updateToken(EnphaseJWTExtractor.fetchJWTV2(authorisationToken.getUser(), authorisationToken.getPassword(), authorisationToken.getSerialNo()));
				secureTemplate = createSecureRestTemplateV7();
			} else {
				log.debug("Creating a new secure V7 access template with provided token");
				secureTemplate = createSecureRestTemplateV7();
			}
		} else if (authorisationToken.hasExpired()) {
			if (authorisationToken.canFetchToken()) {
				log.debug("Creating a new secure V7 access template after refreshing token from Enphase");
				authorisationToken.updateToken(EnphaseJWTExtractor.fetchJWTV2(authorisationToken.getUser(), authorisationToken.getPassword(), authorisationToken.getSerialNo()));
				secureTemplate = createSecureRestTemplateV7();
			} else {
				log.error("Token has expired.  Please update JWT and restart");
			}
		}

		return secureTemplate;
	}

	public RestTemplate getDefaultTemplate() {
		if (defaultTemplate == null) {
			log.debug("Creating a new default access template");
			defaultTemplate = createDefaultRestTemplate();
		}
		return defaultTemplate;
	}

	// Installer Provider is also likely changed with V7
	private RestTemplate getInstallerTemplate() throws URISyntaxException {
		if (installerTemplate == null) {
			installerTemplate = createSecureRestTemplateV5(installerProvider());
		}
		return installerTemplate;
	}

	public String getExpiryAsString() {
		return authorisationToken.isV5() ? "Never" : authorisationToken.getExpires().format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

}
