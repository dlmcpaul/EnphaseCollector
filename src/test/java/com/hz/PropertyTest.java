package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

@SpringBootTest
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class PropertyTest {

	@Autowired
	private EnphaseCollectorProperties properties;

	@Test
	void preferredValueTest() {
		// Test non-zero value returns the input
		assertThat(BigDecimal.valueOf(1), comparesEqualTo(properties.getRefreshAsMinutes(BigDecimal.valueOf(1))));
		assertThat(BigDecimal.valueOf(0.25), comparesEqualTo(properties.getRefreshAsMinutes(BigDecimal.valueOf(0.25))));

		// Test zero value returns the property value
		assertThat(BigDecimal.ONE, comparesEqualTo(properties.getRefreshAsMinutes(BigDecimal.valueOf(0))));
		assertThat(BigDecimal.ONE, comparesEqualTo(properties.getRefreshAsMinutes(BigDecimal.ZERO)));
	}

	@Test
	void refreshSecondsTest() {
		properties.setRefreshSeconds(60000);
		assertThat(properties.getRefreshSeconds(), comparesEqualTo(60000));

		properties.setRefreshSeconds(60);
		assertThat(properties.getRefreshSeconds(), comparesEqualTo(60000));
		assertThat(properties.getRefreshAsMinutes(), comparesEqualTo(BigDecimal.ONE));
		assertThat(properties.getRefreshAsMinutes(BigDecimal.ZERO), comparesEqualTo(BigDecimal.ONE));
		assertThat(properties.getRefreshAsMinutes(BigDecimal.TEN), comparesEqualTo(BigDecimal.TEN));

		properties.setRefreshSeconds(30);
		assertThat(properties.getRefreshSeconds(), comparesEqualTo(30000));
		assertThat(properties.getRefreshAsMinutes(), comparesEqualTo(BigDecimal.valueOf(0.5)));
	}
}
