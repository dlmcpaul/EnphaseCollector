package com.hz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Convertors {

	private Convertors() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(Convertors.class);

	public static BigDecimal convertToWattHours(BigDecimal watts, int minutesOfOperation) {
		// Wh = W * hours of operation
		// So scale minutes of operation to hours of operation

		return watts.multiply(BigDecimal.valueOf(minutesOfOperation)).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
	}

	public static BigDecimal convertToKiloWattHours(BigDecimal watts, int minutesOfOperation) {
		BigDecimal wattHours = Convertors.convertToWattHours(watts, minutesOfOperation);
		return wattHours.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
	}

	public static BigDecimal convertToKiloWattHours(Long watts, int minutesOfOperation) {
		return convertToKiloWattHours(BigDecimal.valueOf(watts), minutesOfOperation);
	}
}
