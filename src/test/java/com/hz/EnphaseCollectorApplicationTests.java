package com.hz;

import com.hz.models.envoy.xml.EnvoyInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("testing")
class EnphaseCollectorApplicationTests {

	@Mock
	private EnvoyInfo envoyInfo;

	@Test
	public void contextLoads() {
		Assertions.assertTrue(true);
	}

}
