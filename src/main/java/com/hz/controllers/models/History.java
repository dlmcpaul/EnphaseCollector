package com.hz.controllers.models;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.models.database.Summary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class History {
	BigDecimal baseCost = BigDecimal.ZERO;
	BigDecimal importCost = BigDecimal.ZERO;
	BigDecimal exportEarnings = BigDecimal.ZERO;
	BigDecimal importSavings = BigDecimal.ZERO;
	BigDecimal billEstimate = BigDecimal.ZERO;

	List<FloatValue> production = new ArrayList<>();
	List<FloatValue> consumption = new ArrayList<>();
	List<FloatValue> gridImport = new ArrayList<>();
	List<FloatValue> gridExport = new ArrayList<>();
	List<FloatValue> solarConsumption = new ArrayList<>();

	public void addSummary(Summary summary, EnphaseCollectorProperties properties) {
		consumption.add(new FloatValue(summary.getDate(), summary.getConsumption()));
		production.add(new FloatValue(summary.getDate(), summary.getProduction()));
		gridImport.add(new FloatValue(summary.getDate(), summary.getGridImport()));
		gridExport.add(new FloatValue(summary.getDate(), summary.getGridExport()));
		solarConsumption.add(new FloatValue(summary.getDate(), calculateSolarConsumption(summary.getConsumption(), summary.getGridImport())));

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
}
