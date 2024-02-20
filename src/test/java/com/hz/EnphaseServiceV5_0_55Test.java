package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnvoyConnectionProxy;
import com.hz.services.EnvoyService;
import com.hz.utils.MetricCalculatorNegativeConsumption;
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
@AutoConfigureWireMock(port = 0,stubs="classpath:/stubs/D5.0.55")
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
@Log4j2
@ExtendWith(MockitoExtension.class)
class EnphaseServiceV5_0_55Test {

	@TestConfiguration
	static class EnphaseServiceTestContextConfiguration {

		@Bean
		@Primary
		String mockEnvoyInfo() {
			return "<?xml version='1.0' encoding='UTF-8'?>\n<envoy_info>\n  <time>1673211285</time>\n  <device>\n    <sn>202114XXXXXX</sn>\n    <pn>800-00655-r09</pn>\n    <software>D5.0.55</software>\n    <euaid>4c8675</euaid>\n    <seqnum>0</seqnum>\n    <apiver>1</apiver>\n    <imeter>true</imeter>\n  </device>\n  <package name='rootfs'>\n    <pn>500-00001-r01</pn>\n    <version>02.00.00</version>\n    <build>950</build>\n  </package>\n  <package name='kernel'>\n    <pn>500-00011-r01</pn>\n    <version>04.01.15</version>\n    <build>1905ae</build>\n  </package>\n  <package name='boot'>\n    <pn>590-00019-r01</pn>\n    <version>02.00.01</version>\n    <build>1f421b</build>\n  </package>\n  <package name='app'>\n    <pn>500-00002-r01</pn>\n    <version>05.00.55</version>\n    <build>4f2662</build>\n  </package>\n  <package name='devimg'>\n    <pn>500-00004-r01</pn>\n    <version>01.02.293</version>\n    <build>9cf065</build>\n  </package>\n  <package name='geo'>\n    <pn>500-00008-r01</pn>\n    <version>02.01.22</version>\n    <build>2faa48</build>\n  </package>\n  <package name='backbone'>\n    <pn>500-00010-r01</pn>\n    <version>05.00.02</version>\n    <build>4fe435</build>\n  </package>\n  <package name='meter'>\n    <pn>500-00013-r01</pn>\n    <version>03.02.07</version>\n    <build>276642</build>\n  </package>\n  <package name='agf'>\n    <pn>500-00012-r01</pn>\n    <version>02.02.00</version>\n    <build>8969f6</build>\n  </package>\n  <package name='security'>\n    <pn>500-00016-r01</pn>\n    <version>02.00.00</version>\n    <build>54a6dc</build>\n  </package>\n  <build_info>\n    <build_time_gmt>1607601617</build_time_gmt>\n    <build_id>release-5.0.x-106-Dec-10-20-02:30:12</build_id>\n  </build_info>\n</envoy_info>";
		}

		@Autowired
		private Environment environment;

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
	void enphase_5_0_55_ServiceTest() throws IOException, URISyntaxException {

		Mockito.when(this.envoyConnectionProxy.getSecureTemplate()).thenReturn(enphaseSecureRestTemplate);

		Optional<System> system = this.enphaseService.collectEnphaseData();
		assertTrue(system.isPresent());
		assertEquals("D5.0.55", this.envoyInfo.getSoftwareVersion() );
		assertEquals(19, system.get().getProduction().getInverter().orElseThrow().getActiveCount());
		assertEquals(BigDecimal.valueOf(48718.422), system.get().getProduction().getProductionEim().orElseThrow().getWattsLifetime());
		assertEquals(BigDecimal.valueOf(1288.056), system.get().getProduction().getProductionWatts());
		assertEquals(BigDecimal.valueOf(-266.115), system.get().getProduction().getConsumptionWatts());
		assertEquals(BigDecimal.valueOf(-1554.171), system.get().getProduction().getNetConsumptionWatts());
		assertEquals(0, system.get().getProduction().getBatteryList().size());
		assertTrue(this.enphaseService.isOk());

		MetricCalculator metricCalculator = new MetricCalculatorNegativeConsumption();
		List<Metric> metrics = metricCalculator.calculateMetrics(system.get());

		assertEquals(50, metrics.size());
		assertEquals(-1554.1710205078125, metrics.stream().filter(metric -> metric.getName().equalsIgnoreCase(Metric.METRIC_SOLAR_DIFFERENCE)).findFirst().orElseThrow().getValue());

		assertFalse(envoyInfo.isV7orAbove());
	}

}
