package com.hz;

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
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/D4.5.79")
@ActiveProfiles("testing")
class EnphaseServiceRest_4_5_79_Test {

	@TestConfiguration
	static class EmployeeServiceImplTestContextConfiguration {

		@Autowired
		private Environment environment;

		@Autowired
		private RestTemplateBuilder restTemplateBuilder;

		@Bean
		public EnvoyInfo envoyInfo(Unmarshaller enphaseMarshaller) {
			try {
				return (EnvoyInfo) enphaseMarshaller.unmarshal(new StringSource("<?xml version='1.0' encoding='UTF-8'?>\n<envoy_info>\n  <time>1555161655</time>\n  <device>\n    <sn>121703XXXXXX</sn>\n    <pn>800-00554-r03</pn>\n    <software>D4.5.79</software>\n        <euaid>4c8675</euaid>\n        <seqnum>0</seqnum>\n        <apiver>1</apiver>\n        <imeter>true</imeter>\n      </device>\n      <package name='rootfs'>\n        <pn>500-00001-r01</pn>\n        <version>02.00.00</version>\n        <build>938</build>\n      </package>\n      <package name='kernel'>\n        <pn>500-00011-r01</pn>\n        <version>04.00.00</version>\n        <build>7f6b66</build>\n      </package>\n      <package name='boot'>\n        <pn>590-00018-r01</pn>\n        <version>02.00.01</version>\n        <build>426697</build>\n      </package>\n      <package name='app'>\n        <pn>500-00002-r01</pn>\n        <version>04.05.79</version>\n        <build>08d137</build>\n      </package>\n      <package name='devimg'>\n        <pn>500-00005-r01</pn>\n        <version>01.02.51</version>\n        <build>904bfb</build>\n      </package>\n      <package name='geo'>\n        <pn>500-00008-r01</pn>\n        <version>02.01.20</version>\n        <build>f9825d</build>\n      </package>\n      <package name='backbone'>\n        <pn>500-00010-r01</pn>\n        <version>04.05.88</version>\n        <build>fd85aa</build>\n      </package>\n      <package name='meter'>\n        <pn>500-00013-r01</pn>\n        <version>03.02.02</version>\n        <build>90fa32</build>\n      </package>\n      <package name='agf'>\n        <pn>500-00012-r01</pn>\n        <version>02.02.00</version>\n        <build>9f04db</build>\n      </package>\n      <package name='full'>\n        <pn>500-00001-r01</pn>\n        <version>02.00.00</version>\n        <build>938</build>\n      </package>\n      <package name='security'>\n        <pn>500-00016-r01</pn>\n        <version>02.00.00</version>\n        <build>54a6dc</build>\n      </package>\n    </envoy_info>"));
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
	public void enphase_4_5_79_ServiceTest() {

		Optional<System> system = this.enphaseService.collectEnphaseData();
		Assertions.assertTrue(system.isPresent());
		Assertions.assertEquals("D4.5.79", this.envoyInfo.getSoftwareVersion());
		Assertions.assertEquals("121703XXXXXX", this.envoyInfo.getSerialNumber());
		Assertions.assertEquals(16, system.get().getProduction().getMicroInvertorsList().size());
		Assertions.assertEquals(BigDecimal.valueOf(13337263.955), system.get().getProduction().getProductionEim().get().getWattsLifetime());
		Assertions.assertEquals(BigDecimal.valueOf(1.326), system.get().getProduction().getProductionWatts());
		Assertions.assertEquals(0, system.get().getProduction().getBatteryList().size());
		Assertions.assertTrue(this.enphaseService.isOk());
		Assertions.assertTrue(system.get().getWireless().isSupported());
		Assertions.assertEquals("connected", system.get().getWireless().getCurrentNetwork().getStatus());

		List<Metric> metrics = this.enphaseService.getMetrics(system.get());

		Assertions.assertEquals(24, metrics.size());
	}

}
