package com.hz.interfaces;

import com.hz.metrics.Metric;

import java.time.LocalDateTime;
import java.util.List;

public interface ExportServiceInterface {
	public void sendMetrics(List<Metric> metrics, LocalDateTime readTime);
}
