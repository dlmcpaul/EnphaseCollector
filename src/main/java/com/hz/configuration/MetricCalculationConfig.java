package com.hz.configuration;

import com.hz.interfaces.MetricCalculator;
import com.hz.utils.MetricCalculatorNegativeConsumption;
import com.hz.utils.MetricCalculatorStandard;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Log4j2
public class MetricCalculationConfig {

	@Bean
	@Profile({"!experimental"})
	public MetricCalculator standard() {
		return new MetricCalculatorStandard();
	}

	@Bean
	@Profile({"experimental"})
	public MetricCalculator experimental() {
		return new MetricCalculatorNegativeConsumption();
	}
}
