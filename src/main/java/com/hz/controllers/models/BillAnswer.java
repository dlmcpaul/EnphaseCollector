package com.hz.controllers.models;

import com.hz.models.database.ElectricityRate;
import com.hz.models.database.Summary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
public class BillAnswer {
	private long totalSummaries = 0;

	private BillResult billEstimate = new BillResult();
	private BillResult comparisonEstimate = new BillResult();
	private Usage usage = new Usage();
	private long daysInPeriod = 0;

	public BillAnswer(long daysInPeriod) {
		this.daysInPeriod = daysInPeriod;
	}

	public void addSummary(Summary summary, ElectricityRate electricityRate, BillQuestion billQuestion) {

		totalSummaries++;

		usage.addTotalProduction(summary.getProduction());
		usage.addGridConsumption(summary.getGridImport());
		usage.addSolarConsumption(calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()));
		usage.addTotalConsumption(summary.getConsumption());
		usage.addTotalExported(summary.getGridExport());

		// Calculate estimated Bill costs
		billEstimate.calculateCosts(summary, electricityRate, calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()));
		comparisonEstimate.calculateCosts(summary, billQuestion, calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()));
	}

	// Calculate how much of our consumption is from solar
	private BigDecimal calculateSolarConsumption(BigDecimal consumption, BigDecimal gridImport) {
		return consumption.subtract(gridImport);
	}

	public String showTotalDays() {
		if (daysInPeriod == totalSummaries) {
			return String.valueOf(totalSummaries);
		}

		return String.valueOf(totalSummaries) + " missing " + (daysInPeriod - totalSummaries);
	}
}
