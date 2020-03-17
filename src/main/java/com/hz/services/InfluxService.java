package com.hz.services;

import com.hz.models.Events.MetricCollectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
@Profile("influxdb")
public class InfluxService {
	private final InfluxDB destinationInfluxDB;

	@EventListener
	public void MetricListener(MetricCollectionEvent metricCollectionEvent) {
		log.debug("Writing metric stats at {} with {} items to influxDB", metricCollectionEvent.getCollectionTime(), metricCollectionEvent.getMetrics().size());
		metricCollectionEvent.getMetrics().
				forEach(m -> destinationInfluxDB.write(Point.measurement(m.getName()).time(metricCollectionEvent.getCollectionTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), TimeUnit.MILLISECONDS).addField("value", m.getValue()).build()));
	}
}
