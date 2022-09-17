package com.hz.utils;

import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Log4j2
public class Calculators {

	private Calculators() {
		throw new IllegalStateException("Utility class");
	}

	public static BigDecimal calculateMinutesOfOperation(int microseconds) {
		return BigDecimal.valueOf(microseconds).divide(BigDecimal.valueOf(60000), 3, RoundingMode.HALF_UP);
	}

	public static BigDecimal calculateFinancial(Long recordedWatts, double price, String type, BigDecimal minutesOfOperation) {

		BigDecimal watts = BigDecimal.ZERO;
		final NumberFormat numberInstance = NumberFormat.getNumberInstance();
		final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();

		if (recordedWatts > 0) {
			watts = BigDecimal.valueOf(recordedWatts);
		}

		BigDecimal kiloWattHours = Convertors.convertToKiloWattHours(watts, minutesOfOperation);

		// Convert to dollars cost = KWh * price per kilowatt
		BigDecimal moneyValue = kiloWattHours.multiply(BigDecimal.valueOf(price));

		log.debug("{} - {} calculated from {} Kwh using {} per Kwh and input of {} W ", type, currencyInstance.format(moneyValue), numberInstance.format(kiloWattHours), price, watts);		// NOSONAR

		return moneyValue;
	}

	public static LocalDate calculateStartDateFromDuration(String duration) {
		ChronoUnit unit = ChronoUnit.valueOf(duration.substring(1).toUpperCase());

		return Calculators.calculateStartDateFromDuration(LocalDate.now(), unit, duration);
	}

	public static LocalDate calculateStartDateFromDuration(LocalDate base, ChronoUnit unit, String duration) {
		// We want SUN to SAT as a WEEK instead of MON to SUN
		if (unit.compareTo(ChronoUnit.WEEKS) == 0 && base.getDayOfWeek().equals(DayOfWeek.SUNDAY) == false) {
			base = base.minusDays(base.getDayOfWeek().getValue());
		}
		if (unit.compareTo(ChronoUnit.MONTHS) == 0) {
			base = base.minusDays(base.getDayOfMonth()).plusDays(1);
		}
		return base.minus(Long.parseLong(duration.substring(0,1)), unit);
	}

	public static LocalDate calculateEndDateFromDuration(String duration) {
		ChronoUnit unit = ChronoUnit.valueOf(duration.substring(1).toUpperCase());

		return Calculators.calculateEndDateFromDuration(LocalDate.now(), unit);
	}

	public static LocalDate calculateEndDateFromDuration(LocalDate base, ChronoUnit unit) {
		// We want SUN to SAT as a WEEK instead of MON to SUN
		if (unit.compareTo(ChronoUnit.WEEKS) == 0 && base.getDayOfWeek().equals(DayOfWeek.SUNDAY) == false) {
			base = base.minusDays(base.getDayOfWeek().getValue());
		}
		if (unit.compareTo(ChronoUnit.MONTHS) == 0) {
			return base.minusDays(base.getDayOfMonth());
		}
		return base.minusDays(1);
	}

}
