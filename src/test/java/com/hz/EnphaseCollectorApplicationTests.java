package com.hz;

import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.models.envoy.xml.EnvoyInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class EnphaseCollectorApplicationTests {

	@Test
	void contextLoads(@Autowired EnvoyInfo envoyInfo) {
		Assertions.assertTrue(envoyInfo.getSerialNumber().equalsIgnoreCase("unknown"));
		Assertions.assertFalse(envoyInfo.isV7orAbove());
	}
}
