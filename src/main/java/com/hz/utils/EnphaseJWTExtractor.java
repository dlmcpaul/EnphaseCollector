package com.hz.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hz.models.envoy.LogInResponse;
import com.hz.models.envoy.TokenRequest;
import com.hz.models.envoy.WebToken;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class EnphaseJWTExtractor {

	private static final String ENPHASE_BASE_URI = "https://enlighten.enphaseenergy.com";
	private static final String ENPHASE_TOKEN_URI = "https://entrez.enphaseenergy.com/tokens";

	private EnphaseJWTExtractor() {
		throw new IllegalStateException("Utility class");
	}

	private static String findInputValue(List<Element> inputElements, String name) {
		return inputElements.stream()
				.filter(inputElement -> inputElement.attr("name").equalsIgnoreCase(name))
				.findFirst()
				.map(element -> element.attr("value"))
				.orElse("");
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

	public static String postLogin(CloseableHttpClient httpClient, String username, String password) throws IOException {
		log.info("log into Enphase using POST to enphase login page {}", ENPHASE_BASE_URI + "/login/login.json");
		HttpPost request = new HttpPost(ENPHASE_BASE_URI + "/login/login.json");

		List<BasicNameValuePair> formData = new ArrayList<>();
		formData.add(new BasicNameValuePair("user[email]", username));
		formData.add(new BasicNameValuePair("user[password]", password));

		request.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

		ObjectMapper jsonMapper = new ObjectMapper();
		return jsonMapper.readValue(sendRequest(httpClient, request), LogInResponse.class).getSessionId();
	}

	public static String getToken(CloseableHttpClient httpClient, String serialNumber, String sessionId, String username) throws IOException {
		log.info("Retrieve Token using {}", ENPHASE_TOKEN_URI);
		ObjectMapper jsonMapper = new ObjectMapper();

		HttpPost request = new HttpPost(ENPHASE_TOKEN_URI);
		request.setHeader("Content-Type", "application/json");

		TokenRequest tokenRequest = new TokenRequest(sessionId, serialNumber, username);
		request.setEntity(new StringEntity(jsonMapper.writeValueAsString(tokenRequest)));

		return sendRequest(httpClient, request);
	}

	public static Document getLoginPage(CloseableHttpClient httpClient) throws IOException {
		log.info("Fetching Enlighten Login Page {}", ENPHASE_BASE_URI);
		HttpGet getMethod = new HttpGet(ENPHASE_BASE_URI);
		getMethod.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");

		return httpClient.execute(getMethod, response -> {
			if (response.getCode() != 200) {
				throw new IOException("Failed to load Enlighten Login Page");
			}

			return Jsoup.parse(response.getEntity().getContent(), "UTF-8", ENPHASE_BASE_URI);
		});
	}

	private static UrlEncodedFormEntity encodeFormData(Document document, String username, String password) {
		List<Element> inputElements = document.getElementsByTag("input");

		List<BasicNameValuePair> formData = new ArrayList<>();
		formData.add(new BasicNameValuePair("utf8", findInputValue(inputElements, "utf8")));
		formData.add(new BasicNameValuePair("authenticity_token", findInputValue(inputElements, "authenticity_token")));
		formData.add(new BasicNameValuePair("user[email]", username));
		formData.add(new BasicNameValuePair("user[password]", password));
		formData.add(new BasicNameValuePair("commit", findInputValue(inputElements, "commit")));

		return new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8);
	}

	public static void postForm(CloseableHttpClient httpClient, UrlEncodedFormEntity body) throws IOException {
		log.info("Attempting to Login with a Form Submit");
		HttpPost request = new HttpPost(ENPHASE_BASE_URI + "/login/login");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setHeader("Origin", ENPHASE_BASE_URI);
		request.setEntity(body);

		httpClient.execute(request, response -> {
			if (response.getCode() != 302) {
				throw new IOException("Failed to perform Login");
			}
			HttpGet redirect = new HttpGet(response.getFirstHeader("location").getValue());
			httpClient.execute(redirect, redirectResponse -> {
				log.info("Redirect response {}", redirectResponse.getCode());
				return redirectResponse;
			});
			log.info("SubmitForm Status = {} with redirect to {}", response.getCode(), response.getFirstHeader("location").getValue());
			return response;
		});
	}

	public static String scanForToken(CloseableHttpClient httpClient, String serialNumber) throws IOException {
		log.info("Fetching and Scanning returned HTML for Token");
		HttpGet jwtRequest = new HttpGet(ENPHASE_BASE_URI + "/entrez-auth-token?serial_num=" + serialNumber);

		return httpClient.execute(jwtRequest, response -> {
			if (response.getCode() != 200) {
				throw new IOException("Failed to load the token page");
			}
			Document jwt = Jsoup.parse(response.getEntity().getContent(), "UTF-8", ENPHASE_BASE_URI);
			String tokenObject = jwt.getElementsByTag("body").text();

			if (tokenObject.isEmpty()) {
				log.error("Scan for token failed.  HTML fetched = {}", response.getEntity().getContent());
				throw new IOException("Failed to fetch the token page");
			}

			ObjectMapper jsonMapper = new ObjectMapper();
			return jsonMapper.readValue(tokenObject, WebToken.class).getToken();
		});
	}

	public static String fetchJWTV2(String username, String password, String serialNumber) throws IOException {
		try (CloseableHttpClient httpClient = setupHttpClient()) {
			return getToken(httpClient, serialNumber, postLogin(httpClient, username, password), username);
		}
	}

	public static String fetchJWTV1(String username, String password, String serialNumber) throws IOException {

		try (CloseableHttpClient httpClient = setupHttpClient()) {
			postForm(httpClient, encodeFormData(getLoginPage(httpClient), username, password));
			return scanForToken(httpClient, serialNumber);
		}
	}

}
