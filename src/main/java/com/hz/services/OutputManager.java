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
import java.util.Date;
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

	private Optional<System> system = Optional.empty();

	@Scheduled(fixedRateString = "${envoy.refresh-seconds}")
	public void gather() {
		try {
			system = enphaseService.collectEnphaseData();
			system.ifPresent(s -> influxService.sendMetrics(enphaseService.getMetrics(s), enphaseService.getCollectionTime(s)));
			system.ifPresent(s -> localService.sendMetrics(enphaseService.getMetrics(s), enphaseService.getCollectionTime(s)));
		} catch (Exception e) {
			LOG.error("Failed to collect data from Enphase Controller - {}", e.getMessage());
		}
	}

	public Date getCollectionTime() {
		return system.isPresent() ? enphaseService.getCollectionTime(system.get()) : new Date();
	}

	public List<Metric> getMetrics() {
		return system.isPresent() ? enphaseService.getMetrics( system.get() ) : new ArrayList<>();
	}

	public String getSoftwareVersion() {
		return enphaseService.getVersion();
	}

	public String getSerialNumber() {
		return enphaseService.getSerialNumber();
	}
}
