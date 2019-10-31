package com.hz;

import com.hz.models.envoy.xml.EnvoyInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("testing")
public class EnphaseCollectorApplicationTests {

	@Mock
	private EnvoyInfo envoyInfo;

	@Test
	public void contextLoads() {
		assertTrue(true);
	}

}
