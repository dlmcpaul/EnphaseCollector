package com.hz.controllers.models;

import com.hz.models.database.ElectricityRate;
import com.hz.models.database.Summary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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

	private void add(Summary summary) {
		consumption.add(new FloatValue(summary.getDate(), summary.getConsumption()));
		production.add(new FloatValue(summary.getDate(), summary.getProduction()));
		gridImport.add(new FloatValue(summary.getDate(), summary.getGridImport()));
		gridExport.add(new FloatValue(summary.getDate(), summary.getGridExport()));
		solarConsumption.add(new FloatValue(summary.getDate(), calculateSolarConsumption(summary.getConsumption(), summary.getGridImport())));
	}

	private void sum(Summary summary) {
		int index = consumption.size()-1;
		consumption.get(index).addWatts(summary.getConsumption());
		production.get(index).addWatts(summary.getProduction());
		gridImport.get(index).addWatts(summary.getGridImport());
		gridExport.get(index).addWatts(summary.getGridExport());
		solarConsumption.get(index).addWatts(calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()));
	}

	private boolean isAdd(LocalDate date, String duration) {
		if (consumption.isEmpty() || duration.toLowerCase().contains("days")) {
			return true;
		}

		return ((duration.toLowerCase().contains("weeks") && (date.getDayOfWeek().getValue() == 7))
			|| (duration.toLowerCase().contains("months") && (date.getDayOfMonth() == 1)));
	}

	public void addSummary(Summary summary, ElectricityRate electricityRate, String duration) {

		if (isAdd(summary.getDate(), duration)) {
			add(summary);
		} else {
			sum(summary);
		}

		// Calculate costs and estimated bill
		baseCost = baseCost.add(BigDecimal.valueOf(electricityRate.getDailySupplyCharge()));
		importCost = importCost.add(summary.getGridImport().multiply(BigDecimal.valueOf(electricityRate.getChargePerKiloWatt())));
		exportEarnings = exportEarnings.add(summary.getGridExport().multiply(BigDecimal.valueOf(electricityRate.getPaymentPerKiloWatt())));
		importSavings = importSavings.add(calculateSolarConsumption(summary.getConsumption(), summary.getGridImport()).multiply(BigDecimal.valueOf(electricityRate.getChargePerKiloWatt())));
		billEstimate = baseCost.add(importCost).subtract(exportEarnings);
	}

	// Calculate how much of our consumption is from solar
	private BigDecimal calculateSolarConsumption(BigDecimal consumption, BigDecimal gridImport) {
		return consumption.subtract(gridImport);
	}
}
