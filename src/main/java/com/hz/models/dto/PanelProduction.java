package com.hz.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PanelProduction {
	private BigDecimal maxProduction;
	private BigDecimal totalProduction;
	private long totalPanelsProducingMax;
}
