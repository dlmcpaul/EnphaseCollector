package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.metrics.Metric;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Profile("influxdb")
public class InfluxService implements InfluxExportInterface {
	private static final Logger LOG = LoggerFactory.getLogger(InfluxService.class);

	private final InfluxDB destinationInfluxDB;

	@Autowired
	public InfluxService(InfluxDB destinationInfluxDB) {
		this.destinationInfluxDB = destinationInfluxDB;
	}

	public void sendMetrics(List<Metric> metrics, LocalDateTime readTime) {
		metrics.forEach(m ->	destinationInfluxDB.write(Point.measurement(m.getName()).time(readTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), TimeUnit.MILLISECONDS).addField("value", m.getValue()).build()));
		LOG.debug("wrote measurement with {} fields at {}", metrics.size(), readTime);
	}
}
