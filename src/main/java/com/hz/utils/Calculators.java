package com.hz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class Calculators {

	private static final Logger LOG = LoggerFactory.getLogger(Calculators.class);

	private Calculators() {}

	public static BigDecimal calculateFinancial(Long recordedWatts, double price, String type, int minutesOfOperation) {

		BigDecimal watts = BigDecimal.ZERO;
		final NumberFormat numberInstance = NumberFormat.getNumberInstance();
		final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();

		if (recordedWatts > 0) {
			watts = BigDecimal.valueOf(recordedWatts);
		}

		BigDecimal kiloWattHours = Convertors.convertToKiloWattHours(watts, minutesOfOperation);

		// Convert to dollars cost = KWh * price per kilowatt
		BigDecimal moneyValue = kiloWattHours.multiply(BigDecimal.valueOf(price));

		LOG.debug("{} - {} calculated from {} Kwh using {} per Kwh and input of {} W ", type, currencyInstance.format(moneyValue), numberInstance.format(kiloWattHours), price, watts);		// NOSONAR

		return moneyValue;
	}

}
