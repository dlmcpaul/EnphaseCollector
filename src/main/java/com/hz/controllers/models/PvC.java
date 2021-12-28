package com.hz.controllers.models;

import com.hz.models.database.Event;
import com.hz.models.database.PanelSummary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PvC {
	List<IntValue> production = new ArrayList<>();
	List<IntValue> consumption = new ArrayList<>();
	List<IntValue> gridImport = new ArrayList<>();
	List<IntValue> excess = new ArrayList<>();
	List<PlotBand> plotBands = new ArrayList<>();

	public void addEvent(Event event) {
		consumption.add(new IntValue(event.getTime(), event.getConsumption().multiply(BigDecimal.valueOf(-1))));
		production.add(new IntValue(event.getTime(), event.getProduction()));
		gridImport.add(new IntValue(event.getTime(), calculateGridUsage(event.getProduction(),event.getConsumption())));
	}

	private BigDecimal calculateGridUsage(BigDecimal production, BigDecimal consumption) {
		if (production.compareTo(consumption) < 0) {
			return consumption.subtract(production);
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal calculateExcess(BigDecimal production, BigDecimal consumption, BigDecimal exportLimit) {
		// Are we producing more power than we can export
		if (production.compareTo(exportLimit) > 0) {
			// Are we importing from the grid
			if (calculateGridUsage(production, consumption).compareTo(BigDecimal.ZERO) <= 0) {
				// No so we have excess power
				BigDecimal excess = production.subtract(consumption).subtract(exportLimit);
				return excess.compareTo(BigDecimal.ZERO) > 0 ? excess: BigDecimal.ZERO;
			}
		}
		return BigDecimal.ZERO;
	}

	public void generateExcess(List<PanelSummary> panelSummaries, int exportLimit) {
		if (exportLimit > 0) {
			panelSummaries.forEach(panelSummary -> excess.add(new IntValue(panelSummary.getTime(), calculateExcess(panelSummary.getProduction(), panelSummary.getConsumption(), BigDecimal.valueOf(exportLimit)))));
		}
	}
}
