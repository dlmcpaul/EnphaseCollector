package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

class HTTPResourceTest {

	private EnphaseCollectorProperties.HTTPResource makeHTTPResource(String host, int port, String context) {
		EnphaseCollectorProperties.HTTPResource resource = new EnphaseCollectorProperties.HTTPResource();
		resource.setHost(host);
		resource.setPort(port);
		resource.setContext(context);

		return resource;
	}

	@Test
	void URLPort80Test() {
		EnphaseCollectorProperties.HTTPResource resource = makeHTTPResource("hzmega.local", 80, "");
		assertThat(resource.getUrl(), comparesEqualTo("http://hzmega.local"));
		assertThat(resource.getUnencryptedUrl(), comparesEqualTo("http://hzmega.local"));
	}

	@Test
	void URLPort443Test() {
		EnphaseCollectorProperties.HTTPResource resource = makeHTTPResource("hzmega.local", 443, "");
		assertThat(resource.getUrl(), comparesEqualTo("https://hzmega.local"));
		assertThat(resource.getEncryptedUrl(), comparesEqualTo("https://hzmega.local"));
	}

	@Test
	void URLPort8080Test() {
		EnphaseCollectorProperties.HTTPResource resource = makeHTTPResource("hzmega.local", 8080, "");
		assertThat(resource.getUrl(), comparesEqualTo("http://hzmega.local:8080"));
	}

	@Test
	void URLContextTest() {
		EnphaseCollectorProperties.HTTPResource resource = makeHTTPResource("hzmega.local", 80, "solar");
		assertThat(resource.getUrl(), comparesEqualTo("http://hzmega.local/solar"));

		resource.setContext(null);
		assertThat(resource.getUrl(), comparesEqualTo("http://hzmega.local"));
		resource.setContext("");
		assertThat(resource.getUrl(), comparesEqualTo("http://hzmega.local"));
	}

}
