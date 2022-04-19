package com.hz.services;

import com.hz.metrics.Metric;
import com.hz.models.events.MetricCollectionEvent;
import io.micrometer.core.instrument.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class PrometheusService {

	private MetricCollectionEvent metricCollectionEvent = new MetricCollectionEvent(this, LocalDateTime.now(), new ArrayList<Metric>());
	private MeterRegistry registry;

	private double getMetric(String name) {
		return this.metricCollectionEvent.getMetrics()
				.stream()
				.filter(metric -> metric.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(metric -> BigDecimal.valueOf(metric.getValue()))
				.orElse(BigDecimal.ZERO).doubleValue();
	}

	public PrometheusService(MeterRegistry registry) {

		this.registry = registry;

		TimeGauge.builder("solar.collection.time", this, TimeUnit.MILLISECONDS, value -> this.metricCollectionEvent.getCollectionTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
				.tags(Tags.of(Tag.of("TZ", ZoneId.systemDefault().getId())))
				.description("Collection time")
				.register(registry);

		Gauge.builder("solar.meter.production", this, value -> getMetric("solar.production.current"))
				.baseUnit("Watts")
				.description("Solar production as at the collection time")
				.register(registry);

		Gauge.builder("solar.meter.consumption", this, value -> getMetric("solar.consumption.current"))
				.baseUnit("Watts")
				.description("Household consumption as at the collection time")
				.register(registry);

		Gauge.builder("solar.meter.voltage", this, value -> getMetric("solar.production.voltage"))
				.baseUnit("Volts")
				.description("Production Voltage as at the collection time")
				.register(registry);
	}

	@EventListener
	public void metricListener(MetricCollectionEvent metricCollectionEvent) {
		log.info("Caching metric stats at {} with {} items for Prometheus consumption", metricCollectionEvent.getCollectionTime(), metricCollectionEvent.getMetrics().size());
		this.metricCollectionEvent = metricCollectionEvent;

		if (registry.find("solar.panel.production").gauge() == null) {
			this.metricCollectionEvent.getMetrics()
					.stream()
					.filter(Metric::isSolarPanel)
					.forEach(panel -> Gauge.builder("solar.panel.production", this, value -> getMetric(panel.getName()))
							.tag("panel.id", panel.getName())
							.baseUnit("Watts")
							.description("Panel Production")
							.register(registry));
		}
	}
}
