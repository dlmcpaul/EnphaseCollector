package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnvoyConnectionProxy;
import com.hz.services.EnvoyService;
import com.hz.utils.MetricCalculatorStandard;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/D4.2.27")
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
@Log4j2
@ExtendWith(MockitoExtension.class)
class EnphaseServiceV4_2_27_Test {

	@TestConfiguration
	static class EnphaseServiceTestContextConfiguration {

		@Autowired
		private Environment environment;

		@Bean
		@Primary
		String mockEnvoyInfo() {
			return "<?xml version='1.0' encoding='UTF-8'?>\n<envoy_info>\n  <time>1557924663</time>\n  <device>\n    <sn>121617XXXXXX</sn>\n    <pn>800-00547-r05</pn>\n    <software>D4.2.27</software>\n    <euaid>4c8675</euaid>\n    <seqnum>0</seqnum>\n    <apiver>1</apiver>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>922</build>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>922</build>\n    <pn>500-00011-r01</pn>\n    <version>04.00.00</version>\n    <build>26b550</build>\n    <pn>590-00018-r01</pn>\n    <version>02.00.01</version>\n    <build>1d81f5</build>\n    <pn>500-00002-r01</pn>\n    <version>04.02.27</version>\n    <build>b13066</build>\n    <pn>500-00004-r01</pn>\n    <version>01.01.41</version>\n    <build>00ecd8</build>\n    <pn>500-00008-r01</pn>\n    <version>01.05.07</version>\n    <build>eb932f</build>\n    <pn>500-00010-r01</pn>\n    <version>04.02.43</version>\n    <build>a3ade0</build>\n    <pn>500-00013-r01</pn>\n    <version>02.01.04</version>\n    <build>299acc</build>\n    <pn>500-00012-r01</pn>\n    <version>01.00.00</version>\n    <build>b13066</build>\n    <pn>500-00016-r01</pn>\n    <version>02.00.00</version>\n    <build>54a6dc</build>\n  </device>\n</envoy_info>";
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
	void enphase_4_2_27_ServiceTest() throws IOException, URISyntaxException {

		Mockito.when(this.envoyConnectionProxy.getSecureTemplate()).thenReturn(enphaseSecureRestTemplate);

		Optional<System> system = this.enphaseService.collectEnphaseData();
		assertTrue(system.isPresent());
		assertEquals("D4.2.27", this.envoyInfo.getSoftwareVersion() );
		assertEquals(20, system.get().getProduction().getInverter().get().getActiveCount());
		assertEquals(BigDecimal.valueOf(12605195.311), system.get().getProduction().getProductionEim().get().getWattsLifetime());
		assertEquals(BigDecimal.valueOf(-1.707), system.get().getProduction().getProductionWatts());
		assertEquals(0, system.get().getProduction().getBatteryList().size());
		assertTrue(this.enphaseService.isOk());

		MetricCalculator metricCalculator = new MetricCalculatorStandard();
		List<Metric> metrics = metricCalculator.calculateMetrics(system.get());

		assertEquals(25, metrics.size());

		assertFalse(envoyInfo.isV7orAbove());
	}

}
