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
	List<Value> production = new ArrayList<>();
	List<Value> consumption = new ArrayList<>();
	List<Value> gridimport = new ArrayList<>();

	public void addEvent(Event event) {
		consumption.add(new Value(event.getTime(), event.getConsumption().multiply(BigDecimal.valueOf(-1))));
		production.add(new Value(event.getTime(), event.getProduction()));
		gridimport.add(new Value(event.getTime(), calculateDiff(event.getProduction(),event.getConsumption())));
	}

	private BigDecimal calculateDiff(BigDecimal production, BigDecimal consumption) {
		if (production.compareTo(consumption) < 0) {
			return consumption.subtract(production);
		}

		return BigDecimal.ZERO;
	}
}
