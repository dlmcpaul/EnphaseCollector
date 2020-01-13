package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.InfluxExportInterface;
import com.hz.interfaces.PvOutputExportInterface;
import com.hz.metrics.Metric;
import com.hz.models.database.ElectricityRate;
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

import java.time.LocalDate;
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
	private final LocalDBService localExportService;
	private final PvOutputExportInterface pvoutputExportService;
	private final EnvoyInfo envoyInfo;
	private final EnphaseCollectorProperties properties;

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		log.info("Application Started");
		this.gather();
		this.upgradeRates();
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

	private void upgradeRates() {
		ElectricityRate rate = localExportService.getRateForDate(LocalDate.now());
		if (rate == null) {
			// First creation set effective to first summary event
			localExportService.saveElectricityRate(new ElectricityRate(properties.getPaymentPerKiloWatt(), properties.getChargePerKiloWatt(), properties.getDailySupplyCharge()));
		} else if (properties.getEffectiveRateDate() == null && rate.getChargePerKiloWatt().compareTo(properties.getChargePerKiloWatt()) != 0) {
			// Rate has changes set new rate from today
			localExportService.saveElectricityRate(LocalDate.now(), new ElectricityRate(properties.getPaymentPerKiloWatt(), properties.getChargePerKiloWatt(), properties.getDailySupplyCharge()));
		} else if (properties.getEffectiveRateDate().isAfter(LocalDate.now())) {
			// Rate is changing in future
			localExportService.saveElectricityRate(properties.getEffectiveRateDate(), new ElectricityRate(properties.getPaymentPerKiloWatt(), properties.getChargePerKiloWatt(), properties.getDailySupplyCharge()));
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
