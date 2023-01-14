package com.hz.interfaces;

import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;

import java.util.List;

public interface MetricCalculator {
	List<Metric> calculateMetrics(System system);
}
