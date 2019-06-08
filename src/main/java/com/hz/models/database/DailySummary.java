package com.hz.models.database;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySummary {
	public LocalDate getDate();
	public BigDecimal getConsumption();
	public BigDecimal getProduction();
}
