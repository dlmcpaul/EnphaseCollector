package com.hz.configuration;

import com.hz.interfaces.MetricCalculator;
import com.hz.utils.MetricCalculatorNegativeConsumption;
import com.hz.utils.MetricCalculatorStandard;
import com.hz.utils.MetricCalculatorStandardBattery;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.hz.configuration.Profiles.EXPERIMENTAL;
import static com.hz.configuration.Profiles.NOT_EXPERIMENTAL;

@Configuration
@Log4j2
public class MetricCalculationConfig {

	@Bean
	@Profile({NOT_EXPERIMENTAL})
	public MetricCalculator standard(EnphaseCollectorProperties properties) {
		if (properties.isSupportBattery()) {
			return new MetricCalculatorStandardBattery();
		}
		return new MetricCalculatorStandard();
	}

	@Bean
	@Profile({EXPERIMENTAL})
	public MetricCalculator experimental() {
		return new MetricCalculatorNegativeConsumption();
	}
}
