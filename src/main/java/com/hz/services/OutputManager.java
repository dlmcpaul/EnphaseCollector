package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.interfaces.LocalExportInterface;
import com.hz.interfaces.PvOutputExportInterface;
import com.hz.models.database.EnvoySystem;
import com.hz.models.envoy.json.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Profile("!testing")
public class OutputManager {
	private static final Logger LOG = LoggerFactory.getLogger(OutputManager.class);

	private EnphaseService enphaseImportService;
	private InfluxExportInterface influxExportService;
	private LocalExportInterface localExportService;
	private PvOutputExportInterface pvoutputExportService;

	@Autowired
	public OutputManager(EnphaseService enphaseImportService, InfluxExportInterface influxExportService, LocalExportInterface localExportService, PvOutputExportInterface pvoutputExportService) {
		this.enphaseImportService = enphaseImportService;
		this.influxExportService = influxExportService;
		this.localExportService = localExportService;
		this.pvoutputExportService = pvoutputExportService;
	}

	@PostConstruct
	public void init() {
		this.gather();
	}

	// Summarise the Event table at 5 minutes past midnight
	@Scheduled(cron="0 5 0 * * ?")
	public void summariseEvents() {
		LOG.info("Summarising Event table");
		try {
			localExportService.summariseEvents();
		} catch (Exception e) {
			LOG.error("Failed to summarise Event table: {} {}", e.getMessage(), e);
		}
	}

	@Scheduled(fixedRateString = "${envoy.refresh-seconds}")
	public void gather() {
		try {
			Optional<System> system = enphaseImportService.collectEnphaseData();
			system.ifPresent(s -> influxExportService.sendMetrics(enphaseImportService.getMetrics(s), enphaseImportService.getCollectionTime(s)));
			system.ifPresent(s -> localExportService.sendSystemInfo(makeSystemInfo(s)));
			system.ifPresent(s -> localExportService.sendMetrics(enphaseImportService.getMetrics(s), enphaseImportService.getCollectionTime(s)));
			system.ifPresent(s -> pvoutputExportService.sendMetrics(enphaseImportService.getMetrics(s), enphaseImportService.getCollectionTime(s)));
		} catch (Exception e) {
			LOG.error("Failed to collect data from Enphase Controller - {}", e.getMessage(), e);
		}
	}

	private EnvoySystem makeSystemInfo(System system) {
		EnvoySystem envoySystem = new EnvoySystem();
		envoySystem.setEnvoySerial(enphaseImportService.getSerialNumber());
		envoySystem.setEnvoyVersion(enphaseImportService.getSoftwareVersion());

		if (system.getNetwork().isWifi()) {
			envoySystem.setWifi(true);
			envoySystem.setNetwork(system.getWireless().getCurrentNetwork().getSsid());
		}

		envoySystem.setLastCommunication(LocalDateTime.ofInstant(system.getNetwork().getLastReportTime().toInstant(), ZoneId.systemDefault()));
		envoySystem.setLastReadTime(enphaseImportService.getCollectionTime(system));
		envoySystem.setPanelCount(system.getProduction().getMicroInvertorsList().size());

		return envoySystem;
	}

}
