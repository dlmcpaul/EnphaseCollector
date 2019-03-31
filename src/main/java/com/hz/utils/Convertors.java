package com.hz.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Convertors {
	public static BigDecimal convertToWattHours(BigDecimal watts, int refreshMicroSeconds) {
		// Convert to Wh = watts / (60 * 60 * 1000 / refreshMicroSeconds)
		BigDecimal oneHour = BigDecimal.valueOf(60 * 60 * 1000L).divide(BigDecimal.valueOf(refreshMicroSeconds), 4, RoundingMode.HALF_UP);
		return watts.divide(oneHour, 4, RoundingMode.HALF_UP);
	}
}
