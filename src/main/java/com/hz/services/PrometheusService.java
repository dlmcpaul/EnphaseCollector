package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
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
	private static final String WATTS = "watts";
	private static final String VOLTS = "volts";
	private static final String AS_AT_COLLECTION_TIME = " as at the collection time";

	private MetricCollectionEvent metricCollectionEvent = new MetricCollectionEvent(this, LocalDateTime.now(), new ArrayList<>());
	private final MeterRegistry registry;

	private double getMetric(String name) {
		return this.metricCollectionEvent.getMetrics()
				.stream()
				.filter(metric -> metric.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(metric -> BigDecimal.valueOf(metric.getValue()))
				.orElse(BigDecimal.ZERO).doubleValue();
	}

	public PrometheusService(MeterRegistry registry, EnphaseCollectorProperties properties) {

		this.registry = registry;

		TimeGauge.builder("solar.collection.time", this, TimeUnit.MILLISECONDS, value -> this.metricCollectionEvent.getCollectionTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
				.tags(Tags.of(Tag.of("TZ", ZoneId.systemDefault().getId())))
				.description("Collection time")
				.register(registry);

		Gauge.builder("solar.collection.period", this, value -> properties.getRefreshSeconds())
				.baseUnit("ms")
				.description("Time in ms between collection events")
				.register(registry);

		Gauge.builder("solar.meter.production", this, value -> getMetric(Metric.METRIC_PRODUCTION_CURRENT))
				.baseUnit(WATTS)
				.description("Solar production" + AS_AT_COLLECTION_TIME)
				.register(registry);

		Gauge.builder("solar.meter.consumption", this, value -> getMetric(Metric.METRIC_CONSUMPTION_CURRENT))
				.baseUnit(WATTS)
				.description("Household consumption" + AS_AT_COLLECTION_TIME)
				.register(registry);

		Gauge.builder("solar.meter.voltage", this, value -> getMetric(Metric.METRIC_PRODUCTION_VOLTAGE))
				.baseUnit(VOLTS)
				.description("Production Voltage" + AS_AT_COLLECTION_TIME)
				.register(registry);

		Gauge.builder("solar.meter.import", this, value -> getMetric(Metric.METRIC_GRID_IMPORT))
				.baseUnit(WATTS)
				.description("Energy imported from the grid" + AS_AT_COLLECTION_TIME)
				.register(registry);

		Gauge.builder("solar.meter.export", this, value -> getMetric(Metric.METRIC_SOLAR_EXCESS))
				.baseUnit(WATTS)
				.description("Energy exported to the grid" + AS_AT_COLLECTION_TIME)
				.register(registry);
	}

	@EventListener
	public void metricListener(MetricCollectionEvent metricCollectionEvent) {
		log.debug("Caching metric stats at {} with {} items for Prometheus consumption", metricCollectionEvent.getCollectionTime(), metricCollectionEvent.getMetrics().size());
		this.metricCollectionEvent = metricCollectionEvent;

		if (registry.find("solar.panel.production").gauge() == null) {
			this.metricCollectionEvent.getMetrics()
					.stream()
					.filter(Metric::isSolarPanel)
					.forEach(panel -> Gauge.builder("solar.panel.production", this, value -> getMetric(panel.getName()))
							.tag("panel.id", panel.getName())
							.baseUnit(WATTS)
							.description("Solar Panel Production")
							.register(registry));
		}
	}
}
