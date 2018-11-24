package com.hz.services;

import com.hz.metrics.Metric;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class InfluxService {
	private static final Logger LOG = LoggerFactory.getLogger(InfluxService.class);

	private final InfluxDB destinationInfluxDB;

	@Autowired
	public InfluxService(InfluxDB destinationInfluxDB) {
		this.destinationInfluxDB = destinationInfluxDB;
	}

	void sendMetrics(List<Metric> metricList, Date readTime) {
		metricList.forEach(m ->	destinationInfluxDB.write(Point.measurement(m.getName()).time(readTime.getTime(), TimeUnit.MILLISECONDS).addField("value", m.getValue()).build()));
		LOG.info("wrote measurement with {} fields at {}", metricList.size(), readTime);
	}
}
