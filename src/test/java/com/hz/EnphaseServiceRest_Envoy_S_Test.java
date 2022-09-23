package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.services.EnvoyConnectionProxy;
import com.hz.services.EnvoyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/envoy-s")
@Import(TestEnphaseSystemInfoConfig.class)
@ActiveProfiles("testing")
class EnphaseServiceRest_Envoy_S_Test {

	@TestConfiguration
	static class EnphaseServiceTestContextConfiguration {

		@Autowired
		private Environment environment;

		@Autowired
		private RestTemplateBuilder restTemplateBuilder;

		@Bean
		public RestTemplate enphaseRestTemplate() {
			RestTemplate result = restTemplateBuilder
					.rootUri("http://localhost:" + this.environment.getProperty("wiremock.server.port"))
					.setConnectTimeout(Duration.ofSeconds(5))
					.setReadTimeout(Duration.ofSeconds(30))
					.build();
			result.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));
			return result;
		}

		@Bean
		public RestTemplate enphaseSecureRestTemplate() {
			RestTemplate result = restTemplateBuilder
					.rootUri("http://localhost:" + this.environment.getProperty("wiremock.server.port"))
					.setConnectTimeout(Duration.ofSeconds(5))
					.setReadTimeout(Duration.ofSeconds(30))
					.build();
			result.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));
			return result;
		}
	}

	@MockBean
	private EnvoyConnectionProxy envoyConnectionProxy;

	@Autowired
	private EnvoyService enphaseService;

	@Autowired
	private RestTemplate enphaseRestTemplate;

	@Autowired
	private RestTemplate enphaseSecureRestTemplate;

	@Test
	void enphase_envoy_s_no_consumption_ServiceTest() throws IOException {
		Mockito.when(this.envoyConnectionProxy.getSecureTemplate()).thenReturn(enphaseSecureRestTemplate);
		Mockito.when(this.envoyConnectionProxy.getDefaultTemplate()).thenReturn(enphaseRestTemplate);

		Optional<System> system = this.enphaseService.collectEnphaseData();
		Assertions.assertTrue(system.isPresent());
		Assertions.assertTrue(this.enphaseService.isOk());
		Assertions.assertEquals(BigDecimal.ZERO, system.get().getProduction().getConsumptionWatts());
		Assertions.assertEquals(BigDecimal.valueOf(41), system.get().getProduction().getProductionWatts());

		List<Metric> metrics = this.enphaseService.getMetrics(system.get());

		Assertions.assertEquals(23, metrics.size());
	}

}
