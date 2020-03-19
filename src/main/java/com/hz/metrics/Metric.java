package com.hz.metrics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by David on 23-Oct-17.
 */
@Data
public class Metric  implements Serializable {
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
