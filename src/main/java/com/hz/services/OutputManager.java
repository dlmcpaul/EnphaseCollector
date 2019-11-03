package com.hz.services;

import com.hz.interfaces.InfluxExportInterface;
import com.hz.interfaces.LocalExportInterface;
import com.hz.interfaces.PvOutputExportInterface;
import com.hz.metrics.Metric;
import com.hz.models.database.EnvoySystem;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.utils.Convertors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
@Profile("!testing")
public class OutputManager {
	private final EnphaseService enphaseImportService;
	private final InfluxExportInterface influxExportService;
	private final LocalExportInterface localExportService;
	private final PvOutputExportInterface pvoutputExportService;
	private final EnvoyInfo envoyInfo;

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		log.info("Application Started");
		this.gather();
		this.summariseEvents();
	}

	// Summarise the Event table at 5 minutes past midnight
	@Scheduled(cron="0 5 0 * * ?")
	public void summariseEvents() {
		log.info("Summarising Event table");
		try {
			localExportService.summariseEvents();
		} catch (Exception e) {
			log.error("Failed to summarise Event table: {} {}", e.getMessage(), e);
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
			log.error("Failed to collect data from Enphase Controller - {}", e.getMessage(), e);
		}
	}

	private EnvoySystem makeSystemInfo(System system) {
		EnvoySystem envoySystem = new EnvoySystem();
		envoySystem.setEnvoySerial(envoyInfo.getSerialNumber());
		envoySystem.setEnvoyVersion(envoyInfo.getSoftwareVersion());

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
