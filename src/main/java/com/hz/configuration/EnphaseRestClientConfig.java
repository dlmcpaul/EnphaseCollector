package com.hz.configuration;

import com.hz.services.EnphaseService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Random;

import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_SCHEME;

/**
 * Created by David on 22-Oct-17.
 */
@Configuration
public class EnphaseRestClientConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EnphaseRestClientConfig.class);

    public static final String SYSTEM = "/home.json";
    public static final String INVENTORY = "/inventory.json";
    public static final String PRODUCTION = "/production.json";

    // Needs Digest authentication
    public static final String INVERTERS = "/api/v1/production/inverters";
	private static final String REALM = "enphaseenergy.com";

    private final EnphaseCollectorConfig config;

	@Autowired
	public EnphaseRestClientConfig(EnphaseCollectorConfig config) {
		this.config = config;
	}

	private CredentialsProvider provider() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials =
				new UsernamePasswordCredentials(config.getController().getUser(), config.getController().getPassword());
		provider.setCredentials(new AuthScope(ANY_HOST, -1, REALM, ANY_SCHEME), credentials);
		return provider;
	}

	@Bean
	public RestTemplate enphaseRestTemplate(RestTemplateBuilder builder) {

		LOG.info("Reading from Insecure source {}", config.getController().getUrl());

		return builder
				.rootUri(config.getController().getUrl())
				.setConnectTimeout(5000)
				.setReadTimeout(5000)
				.build();
	}

    @Bean
    public RestTemplate enphaseSecureRestTemplate(RestTemplateBuilder builder) {

	    LOG.info("Reading from Protected source {}", config.getController().getUrl());

	    HttpHost host = new HttpHost(config.getController().getHost(), config.getController().getPort(), "http");
	    CloseableHttpClient client = HttpClientBuilder.create().
			    setDefaultCredentialsProvider(provider()).useSystemProperties().build();
	    HttpComponentsClientHttpRequestFactory requestFactory =
			    new HttpComponentsClientHttpRequestFactoryDigestAuth(host, client);

	    return builder.rootUri(config.getController().getUrl())
			    .requestFactory(requestFactory)
			    .setConnectTimeout(5000)
			    .setReadTimeout(5000)
			    .build();
    }

	private class HttpComponentsClientHttpRequestFactoryDigestAuth
			extends HttpComponentsClientHttpRequestFactory {

		private final HttpHost host;

		public HttpComponentsClientHttpRequestFactoryDigestAuth(HttpHost host, HttpClient httpClient) {
			super(httpClient);
			this.host = host;
		}

		@Override
		protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
			return createHttpContext();
		}

		private HttpContext createHttpContext() {
			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate DIGEST scheme object, initialize it and add it to the local auth cache
			DigestScheme digestScheme = new DigestScheme();
			// If we already know the realm name
			digestScheme.overrideParamter("realm", REALM);
			digestScheme.overrideParamter("nonce", Long.toString(new Random().nextLong(), 36));
			authCache.put(host, digestScheme);

			// Add AuthCache to the execution context
			BasicHttpContext localcontext = new BasicHttpContext();
			localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
			return localcontext;
		}
	}

	@Bean
	public EnphaseService enphaseService(RestTemplate enphaseRestTemplate, RestTemplate enphaseSecureRestTemplate, RestTemplate destinationRestTemplate) {
		return new EnphaseService(enphaseRestTemplate, enphaseSecureRestTemplate, destinationRestTemplate);
	}

}
