package com.hz.controllers.models;

import com.hz.models.database.ElectricityRate;
import com.hz.models.database.Summary;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

@NoArgsConstructor
@Data
public class Question {
	// Because this object is passed to and from the browser
	// it needs to conform to RFC3339 Section 5.6
	// which sets the standard for the date wire format
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate fromDate = LocalDate.now().minusYears(1);
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate toDate = LocalDate.now();

	private Double paymentPerKiloWatt = 0.0;
	private Double chargePerKiloWatt = 0.0;
	private Double dailySupplyCharge = 0.0;

	private long totalSummaries = 0;

	private Answer billEstimate = new Answer();
	private Answer comparisonEstimate = new Answer();

	public void addSummary(Summary summary, ElectricityRate electricityRate) {

		totalSummaries++;

		// Calculate estimated Bill costs
		billEstimate.addBaseCost(BigDecimal.valueOf(electricityRate.getDailySupplyCharge()));
		billEstimate.addImportCost(summary.getGridImport().multiply(BigDecimal.valueOf(electricityRate.getChargePerKiloWatt())));
		billEstimate.addExportEarnings(summary.getGridExport().multiply(BigDecimal.valueOf(electricityRate.getPaymentPerKiloWatt())));
		billEstimate.addImportSavings(calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()).multiply(BigDecimal.valueOf(electricityRate.getChargePerKiloWatt())));

		// Calculate comparison costs
		comparisonEstimate.addBaseCost(BigDecimal.valueOf(dailySupplyCharge));
		comparisonEstimate.addImportCost(summary.getGridImport().multiply(BigDecimal.valueOf(chargePerKiloWatt)));
		comparisonEstimate.addExportEarnings(summary.getGridExport().multiply(BigDecimal.valueOf(paymentPerKiloWatt)));
		comparisonEstimate.addImportSavings(calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()).multiply(BigDecimal.valueOf(chargePerKiloWatt)));
	}

	// Calculate how much of our consumption is from solar
	private BigDecimal calculateSolarConsumption(BigDecimal consumption, BigDecimal gridImport) {
		return consumption.subtract(gridImport);
	}

	public long daysInPeriod() {
		return DAYS.between(fromDate, toDate) + 1;
	}

	public String showTotalDays() {
		if (daysInPeriod() == totalSummaries) {
			return String.valueOf(totalSummaries);
		}

		return String.valueOf(totalSummaries) + " missing " + (daysInPeriod() - totalSummaries);
	}

	public String toString() {
		return fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " -> " + toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}
