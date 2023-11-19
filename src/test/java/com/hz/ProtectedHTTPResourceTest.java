package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

class ProtectedHTTPResourceTest {

	private EnphaseCollectorProperties.ProtectedHTTPResource makeProtectedHTTPResource() {
		return new EnphaseCollectorProperties.ProtectedHTTPResource();
	}

	@Test
	void UserPasswordTest() {
		EnphaseCollectorProperties.ProtectedHTTPResource resource = makeProtectedHTTPResource();

		assertThat(resource.isUserPasswordSet(), comparesEqualTo(false));
		assertThat(resource.isUserEmpty(), comparesEqualTo(true));
		assertThat(resource.isPasswordEmpty(), comparesEqualTo(true));

		resource.setUser("");
		assertThat(resource.isUserPasswordSet(), comparesEqualTo(false));
		assertThat(resource.isUserEmpty(), comparesEqualTo(true));

		resource.setPassword("");
		assertThat(resource.isUserPasswordSet(), comparesEqualTo(false));
		assertThat(resource.isPasswordEmpty(), comparesEqualTo(true));

		assertThat(resource.noAuthenticationSet(), comparesEqualTo(true));

		resource.setUser("nobody");
		resource.setPassword("invalid");

		assertThat(resource.isUserPasswordSet(), comparesEqualTo(true));
		assertThat(resource.isUserEmpty(), comparesEqualTo(false));
		assertThat(resource.isPasswordEmpty(), comparesEqualTo(false));
		assertThat(resource.noAuthenticationSet(), comparesEqualTo(false));
	}

	@Test
	void TokenTest() {
		EnphaseCollectorProperties.ProtectedHTTPResource resource = makeProtectedHTTPResource();
		assertThat(resource.isTokenEmpty(), comparesEqualTo(true));
		assertThat(resource.isTokenSet(), comparesEqualTo(false));
		assertThat(resource.noAuthenticationSet(), comparesEqualTo(true));

		resource.setToken("");
		assertThat(resource.isTokenEmpty(), comparesEqualTo(true));
		assertThat(resource.isTokenSet(), comparesEqualTo(false));
		assertThat(resource.noAuthenticationSet(), comparesEqualTo(true));

		resource.setToken("ABCDEFG");

		assertThat(resource.isTokenEmpty(), comparesEqualTo(false));
		assertThat(resource.isTokenSet(), comparesEqualTo(true));
		assertThat(resource.noAuthenticationSet(), comparesEqualTo(false));
	}

}
