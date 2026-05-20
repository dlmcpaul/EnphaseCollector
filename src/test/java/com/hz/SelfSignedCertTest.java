package com.hz;

import com.hz.components.EnphaseRequestRetryStrategy;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
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

	private PoolingHttpClientConnectionManager createSSLConnectionManager() {
		// Not good to ignore all the SSL checks
		try {
			SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
					.build();

			var tlsSocketStrategy = new DefaultClientTlsStrategy(
					sslContext,
					HostnameVerificationPolicy.CLIENT,
					NoopHostnameVerifier.INSTANCE);

			return PoolingHttpClientConnectionManagerBuilder.create()
				.setTlsSocketStrategy(tlsSocketStrategy)
				.setDefaultSocketConfig(SocketConfig.custom()
						.setSoTimeout(Timeout.ofMinutes(1))
						.build())
				.setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
				.setConnPoolPolicy(PoolReusePolicy.LIFO)
				.setDefaultConnectionConfig(ConnectionConfig.custom()
						.setSocketTimeout(Timeout.ofMinutes(1))
						.setConnectTimeout(Timeout.ofMinutes(1))
						.setTimeToLive(TimeValue.ofMinutes(10))
						.build())
				.build();
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
				this.createSecureClient().execute(new HttpGet("https://self-signed.badssl.com"),
						response -> response).getCode());
	}

}