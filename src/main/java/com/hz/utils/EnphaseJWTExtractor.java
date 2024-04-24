package com.hz.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hz.models.envoy.LogInResponse;
import com.hz.models.envoy.TokenRequest;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class EnphaseJWTExtractor {

	private static final String ENLIGHTEN_URL = "https://enlighten.enphaseenergy.com";
	private static final String ENTREZ_URL = "https://entrez.enphaseenergy.com";
	private static final String LOGIN_URI = ENLIGHTEN_URL + "/login/login.json";
	private static final String TOKEN_URI = ENTREZ_URL + "/tokens";

	private EnphaseJWTExtractor() {
		throw new IllegalStateException("Utility class");
	}

	private static CloseableHttpClient setupHttpClient() {
		BasicCookieStore cookieStore = new BasicCookieStore();

		return HttpClients
				.custom()
				.useSystemProperties()
				.setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(StandardCookieSpec.STRICT)
						.build())
				.disableRedirectHandling()
				.build();
	}

	private static String sendRequest(CloseableHttpClient httpClient, HttpPost request) throws IOException {
		return httpClient.execute(request, response -> {
			if (response.getCode() != 200) {
				throw new IOException("Failed to post to " + request.getRequestUri() + " returned " + response.getReasonPhrase());
			}
			return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
		});
	}

	private static String postLogin(CloseableHttpClient httpClient, String username, String password) throws IOException {
		log.info("Log into Enphase via enphase login page {}", LOGIN_URI);
		HttpPost request = new HttpPost(LOGIN_URI);

		List<BasicNameValuePair> formData = new ArrayList<>();
		formData.add(new BasicNameValuePair("user[email]", username));
		formData.add(new BasicNameValuePair("user[password]", password));

		request.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

		ObjectMapper jsonMapper = new ObjectMapper();
		return jsonMapper.readValue(sendRequest(httpClient, request), LogInResponse.class).getSessionId();
	}

	private static String getToken(CloseableHttpClient httpClient, String serialNumber, String sessionId, String username) throws IOException {
		log.info("Retrieve Token using entrez token page {}", TOKEN_URI);
		ObjectMapper jsonMapper = new ObjectMapper();

		HttpPost request = new HttpPost(TOKEN_URI);
		request.setHeader("Content-Type", "application/json");
		request.setEntity(new StringEntity(jsonMapper.writeValueAsString(new TokenRequest(sessionId, serialNumber, username))));

		return sendRequest(httpClient, request);
	}

	public static String fetchJWT(String username, String password, String serialNumber) throws IOException {
		try (CloseableHttpClient httpClient = setupHttpClient()) {
			return getToken(httpClient, serialNumber, postLogin(httpClient, username, password), username);
		}
	}

}
