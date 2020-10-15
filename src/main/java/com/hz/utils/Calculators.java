package com.hz.utils;

import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

@Log4j2
public class Calculators {

	private Calculators() {}

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

}
