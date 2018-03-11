package com.hz.metrics;

import lombok.Data;

/**
 * Created by David on 23-Oct-17.
 */
@Data
public class Metric {
	private long time;
	private String name;
	private long value;

	public Metric(long time, String name, long value) {
		this.time = time;
		this.name = name;
		this.value = value < 10 ? 0 : value;
	}

	public Metric(long time, String name, long value1, long value2) {
		this.time = time;
		this.name = name;
		this.value = value1 - value2;
	}

	public String dataPoint() {
		return String.join(" ", name, "value="+String.valueOf(value), String.valueOf(time));
	}
}
