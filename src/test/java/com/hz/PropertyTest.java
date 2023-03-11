package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class PropertyTest {

	@Autowired
	private EnphaseCollectorProperties properties;

	@Test
	void preferredValueTest() {
		// Test non-zero value returns the input
		assertThat(BigDecimal.valueOf(1)).isEqualByComparingTo(properties.getRefreshAsMinutes(BigDecimal.valueOf(1)));
		assertThat(BigDecimal.valueOf(0.25)).isEqualByComparingTo(properties.getRefreshAsMinutes(BigDecimal.valueOf(0.25)));

		// Test zero value returns the property value
		assertThat(BigDecimal.ONE).isEqualByComparingTo(properties.getRefreshAsMinutes(BigDecimal.valueOf(0)));
		assertThat(BigDecimal.ONE).isEqualByComparingTo(properties.getRefreshAsMinutes(BigDecimal.ZERO));
	}
}
