package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
@Log4j2
public class PasswordTest {
	@Autowired
	private EnphaseCollectorProperties properties;

	@Autowired
	private EnvoyInfo envoyInfo;

	@Test
	void testEmptyPassword() {
		assertEquals("nknown", envoyInfo.getDefaultPassword());

		assertTrue(properties.getController().isPasswordEmpty());

		properties.getController().setPassword("test");
		assertFalse(properties.getController().isPasswordEmpty());
	}
}
