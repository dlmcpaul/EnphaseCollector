package com.hz.metrics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by David on 23-Oct-17.
 */
@Data
public class Metric {
	private String name;
	private float value;

	public Metric(String name, float value) {
		this.name = name;
		this.value = value < 10 ? 0 : value;
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
		this.value = value.floatValue() < 10 ? 0 : value.floatValue();
	}
}
