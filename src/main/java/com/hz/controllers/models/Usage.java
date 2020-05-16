package com.hz.controllers.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Usage {
	private BigDecimal totalConsumption = BigDecimal.ZERO;
	private BigDecimal totalProduction = BigDecimal.ZERO;
	private BigDecimal solarConsumption = BigDecimal.ZERO;
	private BigDecimal gridConsumption = BigDecimal.ZERO;
	private BigDecimal totalExported = BigDecimal.ZERO;

	public void addGridConsumption(BigDecimal grid) {
		this.gridConsumption = this.gridConsumption.add(grid);
	}

	public void addSolarConsumption(BigDecimal solar) {
		this.solarConsumption = this.solarConsumption.add(solar);
	}

	public void addTotalConsumption(BigDecimal consumption) {
		this.totalConsumption = this.totalConsumption.add(consumption);
	}

	public void addTotalProduction(BigDecimal production) {
		this.totalProduction = this.totalProduction.add(production);
	}

	public void addTotalExported(BigDecimal exported) {
		this.totalExported = this.totalExported.add(exported);
	}
}
