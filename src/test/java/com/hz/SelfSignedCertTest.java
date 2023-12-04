package com.hz;

import com.hz.components.EnphaseRequestRetryStrategy;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@ExtendWith(SpringExtension.class)
class SelfSignedCertTest {

	private BasicHttpClientConnectionManager createSSLConnectionManager() {
		// Not good to ignore all the SSL checks
		try {
			SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, new TrustSelfSignedStrategy())
					.build();
			Registry<ConnectionSocketFactory> socketFactoryRegistry =
					RegistryBuilder.<ConnectionSocketFactory> create()
							.register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
							.register("http", new PlainConnectionSocketFactory())
							.build();

			return new BasicHttpClientConnectionManager(socketFactoryRegistry);
		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			log.error("Could not create an SSL context - {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private HttpClient createSecureClient() {
		BasicCookieStore cookieStore = new BasicCookieStore();

		return HttpClients
				.custom()
				.useSystemProperties()
				.setRetryStrategy(new EnphaseRequestRetryStrategy())
				.setConnectionManager(createSSLConnectionManager())
				.setDefaultCookieStore(cookieStore)
				.build();

	}

	@Test
	void fetchUrlIgnoringCertChecks() throws IOException {
		assertEquals(200,
				this.createSecureClient().<ClassicHttpResponse>execute(new HttpGet("https://self-signed.badssl.com"),
						response -> {
							return response;
						}).getCode());
	}

}