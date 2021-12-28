package com.hz.models.database;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PanelSummary {
	Long getId();
	LocalDateTime getTime();
	BigDecimal getProduction();
	BigDecimal getConsumption();
}
