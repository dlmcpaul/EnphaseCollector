package com.hz.interfaces;

import com.hz.metrics.Metric;

import java.util.Date;
import java.util.List;

public interface ExportServiceInterface {
	public void sendMetrics(List<Metric> metrics, Date readTime);
}
