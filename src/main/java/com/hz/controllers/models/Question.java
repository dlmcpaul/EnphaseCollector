package com.hz.controllers.models;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.models.database.Summary;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

	private BigDecimal baseCost = BigDecimal.ZERO;
	private BigDecimal importCost = BigDecimal.ZERO;
	private BigDecimal exportEarnings = BigDecimal.ZERO;
	private BigDecimal importSavings = BigDecimal.ZERO;
	private BigDecimal billEstimate = BigDecimal.ZERO;

	public void addSummary(Summary summary, EnphaseCollectorProperties properties) {

		// Calculate costs and estimated bill
		baseCost = baseCost.add(BigDecimal.valueOf(properties.getDailySupplyCharge()));
		importCost = importCost.add(summary.getGridImport().multiply(BigDecimal.valueOf(properties.getChargePerKiloWatt())));
		exportEarnings = exportEarnings.add(summary.getGridExport().multiply(BigDecimal.valueOf(properties.getPaymentPerKiloWatt())));
		importSavings = importSavings.add(calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()).multiply(BigDecimal.valueOf(properties.getChargePerKiloWatt())));
		billEstimate = baseCost.add(importCost).subtract(exportEarnings);
	}

	// Calculate how much of our consumption is from solar
	private BigDecimal calculateSolarConsumption(BigDecimal consumption, BigDecimal gridImport) {
		return consumption.subtract(gridImport);
	}

	public BigDecimal getTotalImportCost() {
		return baseCost.add(importCost);
	}

	public BigDecimal getTotalPayback() {
		return importSavings.add(exportEarnings);
	}

	public String toString() {
		return fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " -> " + toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}
