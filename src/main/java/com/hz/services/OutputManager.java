package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.interfaces.LocalExportInterface;
import com.hz.interfaces.PvOutputExportInterface;
import com.hz.metrics.Metric;
import com.hz.models.database.EnvoySystem;
import com.hz.models.envoy.json.System;
import com.hz.utils.Convertors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
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
		this.summariseEvents();
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

	private void send(System system, List<Metric> metrics, LocalDateTime collectionTime) {
		influxExportService.sendMetrics(metrics, collectionTime);

		localExportService.sendSystemInfo(makeSystemInfo(system));
		localExportService.sendMetrics(metrics, collectionTime);

		pvoutputExportService.sendMetrics(metrics, collectionTime);
	}

	@Scheduled(fixedRateString = "${envoy.refresh-seconds}")
	public void gather() {
		try {
			Optional<System> system = enphaseImportService.collectEnphaseData();

			system.ifPresent(s -> send(s, enphaseImportService.getMetrics(s), enphaseImportService.getCollectionTime(s)));
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

		envoySystem.setLastCommunication(Convertors.convertToLocalDateTime(system.getNetwork().getLastReportTime()));
		envoySystem.setLastReadTime(enphaseImportService.getCollectionTime(system));
		envoySystem.setPanelCount(system.getProduction().getMicroInvertorsList().size());

		return envoySystem;
	}

}
