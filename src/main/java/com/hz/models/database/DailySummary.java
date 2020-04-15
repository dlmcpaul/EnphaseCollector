package com.hz.models.database;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySummary {
	LocalDate getDate();
	BigDecimal getConsumption();
	BigDecimal getProduction();
}
