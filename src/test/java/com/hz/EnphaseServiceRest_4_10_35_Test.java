package com.hz;

import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnphaseService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/R4.10.35")
@ActiveProfiles("testing")
public class EnphaseServiceRest_4_10_35_Test {

	@TestConfiguration
	static class EmployeeServiceImplTestContextConfiguration {

		@Autowired
		private Environment environment;

		@Autowired
		private RestTemplateBuilder restTemplateBuilder;

		@Bean
		public EnvoyInfo envoyInfo(Unmarshaller enphaseMarshaller) {
			return new EnvoyInfo("D4.10.35", "121806XXXXXX");
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

	@Autowired
	private EnvoyInfo envoyInfo;

	@Test
	public void enphase_4_10_35_ServiceTest() {

		Optional<System> system = this.enphaseService.collectEnphaseData();
		Assert.assertTrue(system.isPresent());
		Assert.assertThat(this.envoyInfo.getSoftwareVersion(), Matchers.equalTo("R4.10.35"));
		Assert.assertThat(this.envoyInfo.getSerialNumber(), Matchers.equalTo("121806XXXXXX"));
		Assert.assertThat(system.get().getProduction().getMicroInvertorsList().size(), Matchers.equalTo(41));
		Assert.assertThat(system.get().getProduction().getProductionEim().get().getWattsLifetime(), Matchers.comparesEqualTo(BigDecimal.valueOf(13827622.064)));
		Assert.assertThat(system.get().getProduction().getProductionWatts(), Matchers.comparesEqualTo(BigDecimal.valueOf(1207.430)));
		Assert.assertThat(system.get().getProduction().getBatteryList().size(), Matchers.equalTo(0));
		Assert.assertThat(this.enphaseService.isOk(), Matchers.equalTo(true));
		Assert.assertThat(system.get().getWireless().isSupported(), Matchers.equalTo(true));
		Assert.assertThat(system.get().getWireless().getCurrentNetwork().getStatus(),Matchers.equalTo("connected"));

		List<Metric> metrics = this.enphaseService.getMetrics(system.get());

		Assert.assertThat(metrics.size(), Matchers.equalTo(49));
	}

}
