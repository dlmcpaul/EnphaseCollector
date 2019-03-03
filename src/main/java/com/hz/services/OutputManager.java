package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.interfaces.LocalExportInterface;
import com.hz.models.database.EnvoySystem;
import com.hz.models.envoy.json.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class OutputManager {
	private static final Logger LOG = LoggerFactory.getLogger(OutputManager.class);

	private EnphaseService enphaseService;
	private InfluxExportInterface influxService;
	private LocalExportInterface localService;

	@Autowired
	public OutputManager(EnphaseService enphaseService, InfluxExportInterface influxService, LocalExportInterface localService) {
		this.enphaseService = enphaseService;
		this.influxService = influxService;
		this.localService = localService;
	}

	@PostConstruct
	public void init() {
		this.gather();
	}

	@Scheduled(fixedRateString = "${envoy.refresh-seconds}")
	public void gather() {
		try {
			Optional<System> system = enphaseService.collectEnphaseData();
			system.ifPresent(s -> influxService.sendMetrics(enphaseService.getMetrics(s), enphaseService.getCollectionTime(s)));
			system.ifPresent(s -> localService.sendSystemInfo(makeSystemInfo(s)));
			system.ifPresent(s -> localService.sendMetrics(enphaseService.getMetrics(s), enphaseService.getCollectionTime(s)));
		} catch (Exception e) {
			LOG.error("Failed to collect data from Enphase Controller - {}", e.getMessage(), e);
		}
	}

	private EnvoySystem makeSystemInfo(System system) {
		EnvoySystem envoySystem = new EnvoySystem();
		envoySystem.setEnvoySerial(enphaseService.getSerialNumber());
		envoySystem.setEnvoyVersion(enphaseService.getSoftwareVersion());

		envoySystem.setWifi(system.getNetwork().isWifi());
		envoySystem.setNetwork("");

		envoySystem.setLastCommunication(LocalDateTime.ofInstant(system.getNetwork().getLastReportTime().toInstant(), ZoneId.systemDefault()));
		envoySystem.setLastReadTime(LocalDateTime.ofInstant(enphaseService.getCollectionTime(system).toInstant(), ZoneId.systemDefault()));
		envoySystem.setPanelCount(system.getProduction().getInverterList().size());

		return envoySystem;
	}

}
