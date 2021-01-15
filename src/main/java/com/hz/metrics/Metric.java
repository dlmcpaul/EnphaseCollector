package com.hz.metrics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by David on 23-Oct-17.
 */
@Data
public class Metric  implements Serializable {
	public static final String METRIC_PANEL_NAME_PREFIX = "solar.panel-";
	public static final String METRIC_PRODUCTION_CURRENT = "solar.production.current";
	public static final String METRIC_CONSUMPTION_CURRENT = "solar.consumption.current";
	public static final String METRIC_PRODUCTION_TOTAL = "solar.production.total";
	public static final String METRIC_CONSUMPTION_TOTAL = "solar.consumption.total";
	public static final String METRIC_PRODUCTION_VOLTAGE = "solar.production.voltage";
	public static final String METRIC_SOLAR_EXCESS = "solar.excess";
	public static final String METRIC_SOLAR_SAVINGS = "solar.savings";
	public static final String METRIC_SOLAR_DIFFERENCE = "solar.difference";

	private String name;
	private float value;

	public Metric(String name, float value, int limit) {
		this.name = name;
		this.value = value < limit ? 0 : value;
	}

	public Metric(String name, float value) {
		this.name = name;
		this.value = value;
	}

	public Metric(String name, float value1, float value2) {
		this.name = name;
		this.value = value1 - value2;
	}

	public Metric(String name, BigDecimal value1, BigDecimal value2) {
		this.name = name;
		this.value = value1.subtract(value2).floatValue();
	}

	public Metric(String name, BigDecimal value) {
		this.name = name;
		this.value = value.floatValue();
	}

	public Metric(String name, BigDecimal value, int limit) {
		this.name = name;
		this.value = value.floatValue() < limit ? 0 : value.floatValue();
	}
}
