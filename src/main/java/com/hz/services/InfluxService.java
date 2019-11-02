package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.metrics.Metric;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
@Profile("influxdb")
public class InfluxService implements InfluxExportInterface {
	private final InfluxDB destinationInfluxDB;

	public void sendMetrics(List<Metric> metrics, LocalDateTime readTime) {
		metrics.forEach(m -> destinationInfluxDB.write(Point.measurement(m.getName()).time(readTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), TimeUnit.MILLISECONDS).addField("value", m.getValue()).build()));
		log.debug("wrote measurement with {} fields at {}", metrics.size(), readTime);
	}
}
