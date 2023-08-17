package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.models.envoy.xml.EnvoyInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class EnphaseCollectorApplicationTests {

	@Test
	void contextLoads(@Autowired EnvoyInfo envoyInfo) {
		assertTrue(envoyInfo.getSerialNumber().equalsIgnoreCase("unknown"));
		assertFalse(envoyInfo.isV7orAbove());
	}

	@Test
	void EnvoyInfoTest() {
		EnvoyInfo envoyInfo = new EnvoyInfo("","unknown");
		assertFalse(envoyInfo.isV7orAbove());

		envoyInfo = new EnvoyInfo("D5.0.34","unknown");
		assertFalse(envoyInfo.isV7orAbove());

		envoyInfo = new EnvoyInfo("D7.0.00","unknown");
		assertTrue(envoyInfo.isV7orAbove());
	}
}
