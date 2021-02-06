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
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/D4.2.27")
@Import(TestEnphaseSystemInfoConfig.class)
@ActiveProfiles("testing")
class EnphaseServiceRest_4_2_27_Test {

	@TestConfiguration
	static class EnphaseServiceTestContextConfiguration {

		@Autowired
		private Environment environment;

		@Autowired
		private RestTemplateBuilder restTemplateBuilder;

		@Bean
		@Primary
		public EnvoyInfo envoyInfo(Unmarshaller enphaseMarshaller) {
			try {
				return (EnvoyInfo) enphaseMarshaller.unmarshal(new StringSource("<?xml version='1.0' encoding='UTF-8'?>\n<envoy_info>\n  <time>1557924663</time>\n  <device>\n    <sn>121617XXXXXX</sn>\n    <pn>800-00547-r05</pn>\n    <software>D4.2.27</software>\n    <euaid>4c8675</euaid>\n    <seqnum>0</seqnum>\n    <apiver>1</apiver>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>922</build>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>922</build>\n    <pn>500-00011-r01</pn>\n    <version>04.00.00</version>\n    <build>26b550</build>\n    <pn>590-00018-r01</pn>\n    <version>02.00.01</version>\n    <build>1d81f5</build>\n    <pn>500-00002-r01</pn>\n    <version>04.02.27</version>\n    <build>b13066</build>\n    <pn>500-00004-r01</pn>\n    <version>01.01.41</version>\n    <build>00ecd8</build>\n    <pn>500-00008-r01</pn>\n    <version>01.05.07</version>\n    <build>eb932f</build>\n    <pn>500-00010-r01</pn>\n    <version>04.02.43</version>\n    <build>a3ade0</build>\n    <pn>500-00013-r01</pn>\n    <version>02.01.04</version>\n    <build>299acc</build>\n    <pn>500-00012-r01</pn>\n    <version>01.00.00</version>\n    <build>b13066</build>\n    <pn>500-00016-r01</pn>\n    <version>02.00.00</version>\n    <build>54a6dc</build>\n  </device>\n</envoy_info>"));
			} catch (IOException e) {
				return new EnvoyInfo(e.getMessage(),"");
			}
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
	void enphase_4_2_27_ServiceTest() {

		Optional<System> system = this.enphaseService.collectEnphaseData();
		Assertions.assertTrue(system.isPresent());
		Assertions.assertEquals("D4.2.27", this.envoyInfo.getSoftwareVersion() );
		Assertions.assertEquals(20, system.get().getProduction().getInverter().get().getActiveCount());
		Assertions.assertEquals(BigDecimal.valueOf(12605195.311), system.get().getProduction().getProductionEim().get().getWattsLifetime());
		Assertions.assertEquals(BigDecimal.valueOf(-1.707), system.get().getProduction().getProductionWatts());
		Assertions.assertEquals(0, system.get().getProduction().getBatteryList().size());
		Assertions.assertTrue(this.enphaseService.isOk());

		List<Metric> metrics = this.enphaseService.getMetrics(system.get());

		Assertions.assertEquals(24, metrics.size());
	}

}
