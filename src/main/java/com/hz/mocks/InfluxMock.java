package com.hz.mocks;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.metrics.Metric;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@Profile("!influxdb")
public class InfluxMock implements InfluxExportInterface {

	@Override
	public void sendMetrics(List<Metric> metrics, LocalDateTime readTime) {
		log.debug("Writing stats at {} with {} items", readTime, metrics.size());
	}
}
