package com.hz.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Convertors {

	private Convertors() {
	}

	public static BigDecimal convertToWattHours(BigDecimal watts, BigDecimal minutesOfOperation) {
		// Wh = W * hours of operation
		// So scale minutes of operation to hours of operation

		return watts.multiply(minutesOfOperation).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
	}

	public static BigDecimal convertToKiloWattHours(BigDecimal watts, BigDecimal minutesOfOperation) {
		if (watts == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal wattHours = Convertors.convertToWattHours(watts, minutesOfOperation);
		return wattHours.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
	}

	public static BigDecimal convertToKiloWattHours(Long watts, BigDecimal minutesOfOperation) {
		return convertToKiloWattHours(BigDecimal.valueOf(watts), minutesOfOperation);
	}

	public static LocalDateTime convertToLocalDateTime(long time) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(time * 1000L), ZoneId.systemDefault());
	}

	public static LocalDate convertToLocalDate(long date) {
		return LocalDate.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
	}
}
