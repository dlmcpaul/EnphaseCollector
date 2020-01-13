package com.hz.controllers.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Answer {
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

}
