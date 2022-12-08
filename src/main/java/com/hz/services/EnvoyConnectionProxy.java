package com.hz.services;

import com.hz.components.EnphaseRequestRetryHandler;
import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.models.envoy.AuthorisationToken;
import com.hz.utils.EnphaseJWTExtractor;
import com.hz.utils.InstallerPasswordCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.hz.configuration.EnphaseURLS.*;
import static org.apache.http.auth.AuthScope.ANY_SCHEME;

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

	private RestTemplate buildTemplate(HttpClient httpClient) {
		return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)))
				.build();
	}

	private RestTemplate createDefaultRestTemplate() {
		log.info("Reading from insecure Envoy controller endpoint {}{}", config.getController().getUrl(), SYSTEM);

		HttpClient httpClient = HttpClients
				.custom()
				.useSystemProperties()
				.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
				.build();

		return buildTemplate(httpClient);
	}

	private CredentialsProvider standardProvider() {
		log.info("Preparing Realm Authentication Provider with user {}", config.getController().getUser());

		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
				new UsernamePasswordCredentials(authorisationToken.getUser(), authorisationToken.getPassword());
		provider.setCredentials(new AuthScope(config.getController().getHost(), 80, REALM, ANY_SCHEME), credentials);
		return provider;
	}

	private CredentialsProvider installerProvider() {
		log.info("Preparing Installer Realm Authentication Provider with user {}", InstallerPasswordCalculator.USERNAME);

		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
			new UsernamePasswordCredentials(InstallerPasswordCalculator.USERNAME, InstallerPasswordCalculator.getPassword(authorisationToken.getSerialNo()));
		provider.setCredentials(new AuthScope(config.getController().getHost(), 80, REALM, ANY_SCHEME), credentials);
		return provider;
	}

	private RestTemplate createSecureRestTemplateV5(CredentialsProvider provider) {
		log.info("Reading from protected Envoy controller endpoint {}{}", config.getController().getUrl(), INVERTERS);

		HttpClient httpClient = HttpClients
				.custom()
				.setDefaultCredentialsProvider(provider)
				.useSystemProperties()
				.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
				.build();

		return buildTemplate(httpClient);
	}

	private RestTemplate createSecureRestTemplateV7() {
		Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getBearerToken());

		BasicCookieStore cookieStore = new BasicCookieStore();

		try {
			// Not good to ignore all the SSL checks
			HttpClient httpClient = HttpClients
					.custom()
					.useSystemProperties()
					.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
					.setDefaultHeaders(List.of(header))
					.setSSLContext(SSLContextBuilder.create().loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE).build())
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setDefaultCookieStore(cookieStore)
					.build();

			// Make a call to the /auth/check_jwt endpoint to set the cookie
			HttpResponse response = httpClient.execute(new HttpGet(config.getController().getUrl() + AUTH_CHECK));
			if (response.getStatusLine().getStatusCode() != 200) {
				log.error("Attempt to validate bearer token {} against {} failed with result {}", config.getBearerToken(), config.getController().getUrl() + AUTH_CHECK, response.getStatusLine());
			}

			return buildTemplate(httpClient);

		} catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			log.error("Could not connect to envoy when configuring a v7 http client - {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	public RestTemplate getSecureTemplate() throws IOException {
		if (secureTemplate == null) {
			if (authorisationToken.isV5()) {
				log.debug("Creating a new secure V5 access template");
				secureTemplate = createSecureRestTemplateV5(standardProvider());
			} else if (authorisationToken.canFetchToken()) {
				log.debug("Creating a new secure V7 access template after fetching token from Enphase");
				authorisationToken.updateToken(EnphaseJWTExtractor.fetchJWT(authorisationToken.getUser(), authorisationToken.getPassword(), authorisationToken.getSerialNo()));
				secureTemplate = createSecureRestTemplateV7();
			} else {
				log.debug("Creating a new secure V7 access template with provided token");
				secureTemplate = createSecureRestTemplateV7();
			}
		} else if (authorisationToken.hasExpired()) {
			if (authorisationToken.canFetchToken()) {
				log.debug("Creating a new secure V7 access template after refreshing token from Enphase");
				authorisationToken.updateToken(EnphaseJWTExtractor.fetchJWT(authorisationToken.getUser(), authorisationToken.getPassword(), authorisationToken.getSerialNo()));
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
	private RestTemplate getInstallerTemplate() {
		if (installerTemplate == null) {
			installerTemplate = createSecureRestTemplateV5(installerProvider());
		}
		return installerTemplate;
	}

	public String getExpiryAsString() {
		return authorisationToken.isV5() ? "Never" : authorisationToken.getExpires().format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

}
