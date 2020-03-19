package com.hz.models.events;

import com.hz.metrics.Metric;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.List;

public class MetricCollectionEvent extends ApplicationEvent {
	private final LocalDateTime collectionTime;
	private final List<Metric> metrics;

	public MetricCollectionEvent(Object source, LocalDateTime collectionTime, List<Metric> metrics) {
		super(source);
		this.collectionTime = collectionTime;
		this.metrics = metrics;
	}

	public LocalDateTime getCollectionTime() {
		return collectionTime;
	}

	public List<Metric> getMetrics() {
		return this.metrics;
	}
}
