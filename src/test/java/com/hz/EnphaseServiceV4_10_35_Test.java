package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnvoyConnectionProxy;
import com.hz.services.EnvoyService;
import com.hz.utils.MetricCalculatorStandard;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/R4.10.35")
@Import(TestEnphaseSystemInfoConfig.class)
@ActiveProfiles("testing")
class EnphaseServiceV4_10_35_Test {

	@TestConfiguration
	static class EnphaseServiceTestContextConfiguration {
		@Autowired
		private Environment environment;

		@Bean
		@Primary
		String mockEnvoyInfo() {
			return "<?xml version='1.0' encoding='UTF-8'?>\n<envoy_info>\n  <time>1570542540</time>\n  <device>\n    <sn>121806XXXXXX</sn>\n    <pn>800-00555-r03</pn>\n    <software>R4.10.35</software>\n    <euaid>4c8675</euaid>\n    <seqnum>0</seqnum>\n    <apiver>1</apiver>\n    <imeter>true</imeter>\n  </device>\n  <package name='rootfs'>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>945</build>\n  </package>\n  <package name='kernel'>\n    <pn>500-00011-r01</pn>\n    <version>04.00.00</version>\n    <build>5bb754</build>\n  </package>\n  <package name='boot'>\n    <pn>590-00018-r01</pn>\n    <version>02.00.01</version>\n    <build>426697</build>\n  </package>\n  <package name='app'>\n    <pn>500-00002-r01</pn>\n    <version>04.10.35</version>\n    <build>6ed292</build>\n  </package>\n  <package name='devimg'>\n    <pn>500-00004-r01</pn>\n    <version>01.02.186</version>\n    <build>d0d70f</build>\n  </package>\n  <package name='geo'>\n    <pn>500-00008-r01</pn>\n    <version>02.01.22</version>\n    <build>06e201</build>\n  </package>\n  <package name='backbone'>\n    <pn>500-00010-r01</pn>\n    <version>04.10.25</version>\n    <build>7b7de5</build>\n  </package>\n  <package name='meter'>\n    <pn>500-00013-r01</pn>\n    <version>03.02.07</version>\n    <build>4c9d48</build>\n  </package>\n  <package name='agf'>\n    <pn>500-00012-r01</pn>\n    <version>02.02.00</version>\n    <build>c00a8f</build>\n  </package>\n  <package name='full'>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>945</build>\n  </package>\n  <package name='security'>\n    <pn>500-00016-r01</pn>\n    <version>02.00.00</version>\n    <build>54a6dc</build>\n  </package>\n  <build_info>\n    <build_id>release-4.10.x-103-Nov-12-18-18:25:06</build_id>\n    <build_time_gmt>1542157996</build_time_gmt>\n  </build_info>\n</envoy_info>";
		}

		@Bean
		@Primary
		public String baseUrl() {
			return "http://localhost:" + this.environment.getProperty("wiremock.server.port");
		}

	}

	@MockBean
	private EnvoyConnectionProxy envoyConnectionProxy;

	@Autowired
	private EnvoyService enphaseService;

	@Autowired
	private EnvoyInfo envoyInfo;

	@Autowired
	private RestTemplate enphaseSecureRestTemplate;

	@Test
	void enphase_4_10_35_ServiceTest() throws IOException, URISyntaxException {
		Mockito.when(this.envoyConnectionProxy.getSecureTemplate()).thenReturn(enphaseSecureRestTemplate);

		Optional<System> system = this.enphaseService.collectEnphaseData();
		assertTrue(system.isPresent());
		assertEquals("R4.10.35", this.envoyInfo.getSoftwareVersion());
		assertEquals("121806XXXXXX", this.envoyInfo.getSerialNumber());
		assertEquals(41, system.get().getProduction().getMicroInvertersList().size());
		assertEquals(BigDecimal.valueOf(13827622.064), system.get().getProduction().getProductionEim().get().getWattsLifetime());
		assertEquals(BigDecimal.valueOf(1207430, 3), system.get().getProduction().getProductionWatts());
		assertEquals(BigDecimal.ONE, system.get().getProduction().getPhaseCount());
		assertEquals(BigDecimal.valueOf(246.152), system.get().getProduction().getProductionVoltage());
		assertEquals(0, system.get().getProduction().getBatteryList().size());
		assertTrue(this.enphaseService.isOk());
		assertTrue(system.get().getWireless().isSupported());
		assertEquals("connected", system.get().getWireless().getCurrentNetwork().getStatus());
		assertFalse(this.envoyInfo.isV7orAbove());
		assertFalse(this.envoyInfo.webTokens);

		MetricCalculator metricCalculator = new MetricCalculatorStandard();
		List<Metric> metrics = metricCalculator.calculateMetrics(system.get());

		assertEquals(50, metrics.size());
	}

}
