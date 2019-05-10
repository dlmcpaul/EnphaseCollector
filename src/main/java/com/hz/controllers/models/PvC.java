package com.hz.controllers.models;

import com.hz.models.database.Event;
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
	List<IntValue> gridimport = new ArrayList<>();

	public void addEvent(Event event) {
		consumption.add(new IntValue(event.getTime(), event.getConsumption().multiply(BigDecimal.valueOf(-1))));
		production.add(new IntValue(event.getTime(), event.getProduction()));
		gridimport.add(new IntValue(event.getTime(), calculateDiff(event.getProduction(),event.getConsumption())));
	}

	private BigDecimal calculateDiff(BigDecimal production, BigDecimal consumption) {
		if (production.compareTo(consumption) < 0) {
			return consumption.subtract(production);
		}

		return BigDecimal.ZERO;
	}
}
