package com.hz.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Convertors {

	private Convertors() {}

	public static BigDecimal convertToWattHours(BigDecimal watts, int minutesOfOperation) {
		// Wh = W * hours of operation
		// So scale minutes of operation to hours of operation

		BigDecimal scale = BigDecimal.valueOf(minutesOfOperation).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
		return watts.multiply(scale);
	}
}
