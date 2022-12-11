package com.hz;

import com.hz.components.EnphaseRequestRetryHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@ExtendWith(SpringExtension.class)
class SelfSignedCertTest {

	private HttpClient createSecureClient() {
		BasicCookieStore cookieStore = new BasicCookieStore();

		try {
			// Not good to ignore all the SSL checks
			return HttpClients
					.custom()
					.useSystemProperties()
					.setRetryHandler(new EnphaseRequestRetryHandler(3, true))
					.setSSLContext(SSLContextBuilder.create().loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE).build())
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setDefaultCookieStore(cookieStore)
					.build();

		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			log.error("Could not configure a secure http client - {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Test
	void fetchUrlIgnoringCertChecks() throws IOException {
		HttpClient client = this.createSecureClient();
		HttpUriRequest request = new HttpGet("https://self-signed.badssl.com");

		HttpResponse response = client.execute(request);

		assertEquals(200, response.getStatusLine().getStatusCode());
	}

}