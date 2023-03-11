package com.hz.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hz.models.envoy.WebToken;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
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

	public static CloseableHttpClient setupHttpClient() {
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

	public static Document getLoginPage(CloseableHttpClient httpClient) throws IOException {
		HttpGet getMethod = new HttpGet(ENPHASE_BASE_URI);
		getMethod.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");

		try (CloseableHttpResponse response = httpClient.execute(getMethod)) {

			if (response.getCode() != 200) {
				throw new IOException("Failed to load Enlighten Login Page");
			}

			return Jsoup.parse(response.getEntity().getContent(), "UTF-8", ENPHASE_BASE_URI);
		}
	}

	public static UrlEncodedFormEntity encodeFormData(Document document, String username, String password) {
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
		HttpPost request = new HttpPost(ENPHASE_BASE_URI + "/login/login");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setHeader("Origin", ENPHASE_BASE_URI);
		request.setEntity(body);

		try (CloseableHttpResponse response = httpClient.execute(request)) {
			if (response.getCode() != 302) {
				throw new IOException("Failed to perform Login");
			}
			HttpGet redirect = new HttpGet(response.getFirstHeader("location").getValue());
			try(CloseableHttpResponse redirectResponse = httpClient.execute(redirect)) {
				log.info("Redirect response {}", redirectResponse.getCode());
			}

			log.info("SubmitForm Status = {} with redirect to {}", response.getCode(), response.getFirstHeader("location").getValue());
		}
	}

	public static String scanForToken(CloseableHttpClient httpClient, String serialNumber) throws IOException {
		HttpGet jwtRequest = new HttpGet(ENPHASE_BASE_URI + "/entrez-auth-token?serial_num=" + serialNumber);

		try (CloseableHttpResponse response = httpClient.execute(jwtRequest)) {

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
		}
	}

	public static String fetchJWT(String username, String password, String serialNumber) throws IOException {

		try (CloseableHttpClient httpClient = setupHttpClient()) {
			log.info("Fetching Enlighten Login Page");
			Document loginPage = getLoginPage(httpClient);

			log.info("Attempting to Login with a Form Submit");
			postForm(httpClient, encodeFormData(loginPage, username, password));

			log.info("Fetching and Scanning returned HTML for Token");
			return scanForToken(httpClient, serialNumber);
		}
	}

}
