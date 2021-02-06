package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnphaseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/ThreePhase")
@Import(TestEnphaseSystemInfoConfig.class)
@ActiveProfiles("testing")
class EnphaseServiceRest_ThreePhase_Test {

	@TestConfiguration
	static class EnphaseServiceTestContextConfiguration {

		@Autowired
		private Environment environment;

		@Autowired
		private RestTemplateBuilder restTemplateBuilder;

		@Bean
		public EnvoyInfo envoyInfo(Unmarshaller enphaseMarshaller) {
			return new EnvoyInfo("","");
		}

		@Bean
		public RestTemplate enphaseRestTemplate() {
			return restTemplateBuilder
					.rootUri("http://localhost:" + this.environment.getProperty("wiremock.server.port"))
					.setConnectTimeout(Duration.ofSeconds(5))
					.setReadTimeout(Duration.ofSeconds(30))
					.build();
		}

		@Bean
		public RestTemplate enphaseSecureRestTemplate() {
			return restTemplateBuilder
					.rootUri("http://localhost:" + this.environment.getProperty("wiremock.server.port"))
					.setConnectTimeout(Duration.ofSeconds(5))
					.setReadTimeout(Duration.ofSeconds(30))
					.build();
		}
	}

	@Autowired
	private EnphaseService enphaseService;

	@Test
	void enphase_4_10_35_ServiceTest() {

		Optional<System> system = this.enphaseService.collectEnphaseData();
		Assertions.assertTrue(system.isPresent());
		Assertions.assertEquals(BigDecimal.valueOf(3), system.get().getProduction().getPhaseCount());
		Assertions.assertEquals(BigDecimal.valueOf(242.635), system.get().getProduction().getProductionVoltage());
		Assertions.assertEquals(0, system.get().getProduction().getBatteryList().size());
		Assertions.assertEquals(BigDecimal.valueOf(16112904.995), system.get().getProduction().getProductionEim().get().getWattsLifetime());
		Assertions.assertEquals(BigDecimal.valueOf(8447165, 3), system.get().getProduction().getProductionWatts());
		Assertions.assertTrue(this.enphaseService.isOk());

		List<Metric> metrics = this.enphaseService.getMetrics(system.get());

		Assertions.assertEquals(49, metrics.size());
	}

}
