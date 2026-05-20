package com.hz.models.database;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySummary {
	LocalDate getEachDay();
	BigDecimal getConsumption();
	BigDecimal getProduction();
	BigDecimal getBatteryCharged();
	BigDecimal getBatteryDischarged();
	BigDecimal getGridImport();
	BigDecimal getGridExport();
	BigDecimal getHighestProduction();
	BigDecimal getMinBatterySoc();
	BigDecimal getMaxBatterySoc();
	BigDecimal getMaxCellTemperature();
	BigDecimal getCurrentBatterySoc();
}
