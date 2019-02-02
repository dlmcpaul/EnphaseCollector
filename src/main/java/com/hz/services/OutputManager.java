package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.interfaces.LocalExportInterface;
import com.hz.metrics.Metric;
import com.hz.models.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OutputManager {
	private static final Logger LOG = LoggerFactory.getLogger(OutputManager.class);

	@Autowired
	private EnphaseService enphaseService;

	@Autowired
	private InfluxExportInterface influxService;

	@Autowired
	private LocalExportInterface localService;

	@Scheduled(fixedRateString = "${envoy.refresh-seconds}")
	public void gather() {
		try {
			enphaseService.collectEnphaseData().ifPresent(system -> influxService.sendMetrics(enphaseService.getMetrics(system), enphaseService.getCollectionTime(system)));
			enphaseService.collectEnphaseData().ifPresent(system -> localService.sendMetrics(enphaseService.getMetrics(system), enphaseService.getCollectionTime(system)));
		} catch (Exception e) {
			LOG.error("Failed to collect data from Enphase Controller - {}", e.getMessage());
		}
	}

	public List<Metric> collect() {
		Optional<System> system = enphaseService.collectEnphaseData();
		return system.isPresent() ? enphaseService.getMetrics( system.get() ) : new ArrayList<>();
	}

}
