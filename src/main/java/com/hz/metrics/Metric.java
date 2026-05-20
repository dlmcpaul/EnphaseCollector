package com.hz.metrics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by David on 23-Oct-17.
 */
@Data
public class Metric implements Serializable {
	public static final String METRIC_PANEL_NAME_PREFIX = "solar.panel-";
	public static final String METRIC_PRODUCTION_CURRENT = "solar.production.current";
	public static final String METRIC_CONSUMPTION_CURRENT = "solar.consumption.current";
	public static final String METRIC_PRODUCTION_TOTAL = "solar.production.total";
	public static final String METRIC_CONSUMPTION_TOTAL = "solar.consumption.total";
	public static final String METRIC_PRODUCTION_VOLTAGE = "solar.production.voltage";
	public static final String METRIC_SOLAR_EXCESS = "solar.excess";        // AKA GRID EXPORT
	public static final String METRIC_SOLAR_SAVINGS = "solar.savings";
	public static final String METRIC_GRID_IMPORT = "solar.grid.import";
	public static final String METRIC_SOLAR_DIFFERENCE = "solar.difference";
	public static final String METRIC_BATTERY_AVAILABLE = "battery.available";
	public static final String METRIC_BATTERY_PERCENT = "battery.percent";
	public static final String METRIC_BATTERY_STATE = "battery.state";
	public static final String METRIC_BATTERY_CAPACITY = "battery.capacity";
	public static final String METRIC_BATTERY_WATTS_REMAINING = "battery.watts";
	public static final String METRIC_BATTERY_POWER = "battery.power";
	public static final String METRIC_BATTERY_MAX_TEMPERATURE = "battery.max.temperature";
	public static final String METRIC_BATTERY_MAX_CELL_TEMPERATURE = "battery.max.cell.temperature";
	public static final String METRIC_GRID_TOTAL = "solar.grid.total";

	public static Metric createPanelMetric(String panelId, float value, int limit) {
		return new Metric(METRIC_PANEL_NAME_PREFIX + panelId, value, limit);
	}

	private String name;
	private BigDecimal value;

	public Metric(String name, float value, int limit) {
		this.name = name;
		this.value = BigDecimal.valueOf(value < limit ? 0 : value);
	}

	public Metric(String name, float value) {
		this.name = name;
		this.value = BigDecimal.valueOf(value);
	}

	public Metric(String name, float value1, float value2) {
		this.name = name;
		this.value = BigDecimal.valueOf(value1 - value2);
	}

	public Metric(String name, BigDecimal value1, BigDecimal value2) {
		this.name = name;
		this.value = value1.subtract(value2);
	}

	public Metric(String name, BigDecimal value) {
		this.name = name;
		this.value = value;
	}

	public Metric(String name, BigDecimal value, int limit) {
		this.name = name;
		this.value = value.floatValue() < limit ? BigDecimal.ZERO : value;
	}

	public boolean isSolarPanel() {
		return name.startsWith(Metric.METRIC_PANEL_NAME_PREFIX);
	}

	public boolean isName(String name) {
		return this.name.equalsIgnoreCase(name);
	}
}
