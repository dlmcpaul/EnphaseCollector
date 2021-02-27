package com.hz.controllers.models;

import com.hz.models.database.Summary;
import com.hz.models.interfaces.RateInterface;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillResult {
	private BigDecimal baseCost = BigDecimal.ZERO;
	private BigDecimal importCost = BigDecimal.ZERO;
	private BigDecimal exportEarnings = BigDecimal.ZERO;
	private BigDecimal importSavings = BigDecimal.ZERO;

	public void addBaseCost(BigDecimal value) {
		baseCost = baseCost.add(value);
	}

	public void addImportCost(BigDecimal value) {
		importCost = importCost.add(value);
	}

	public void addExportEarnings(BigDecimal value) {
		exportEarnings = exportEarnings.add(value);
	}

	public void addImportSavings(BigDecimal value) {
		importSavings = importSavings.add(value);
	}

	public BigDecimal getEstimatedBill() {
		return baseCost.add(importCost).subtract(exportEarnings);
	}

	public BigDecimal getTotalImportCost() {
		return baseCost.add(importCost);
	}

	public BigDecimal getTotalPayback() {
		return importSavings.add(exportEarnings);
	}

	public void calculateCosts(Summary summary, RateInterface rate, BigDecimal solarConsumption) {
		this.addBaseCost(BigDecimal.valueOf(rate.getDailySupplyCharge()));
		this.addImportCost(summary.getGridImport().multiply(BigDecimal.valueOf(rate.getChargePerKiloWatt())));
		this.addExportEarnings(summary.getGridExport().multiply(BigDecimal.valueOf(rate.getPaymentPerKiloWatt())));
		this.addImportSavings(solarConsumption.multiply(BigDecimal.valueOf(rate.getChargePerKiloWatt())));
	}
}
