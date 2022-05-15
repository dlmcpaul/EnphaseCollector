package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.metrics.Metric;
import com.hz.models.database.EnvoySystem;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.models.events.MetricCollectionEvent;
import com.hz.models.events.SystemInfoEvent;
import com.hz.utils.Convertors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Profile("!testing")
public class OutputManager {
	private final ApplicationEventPublisher applicationEventPublisher;
	private final EnphaseService enphaseImportService;
	private final EnvoyInfo envoyInfo;
	private final EnphaseCollectorProperties properties;

	private static long collection_period = 0;

	private void publish(System system, List<Metric> metrics, LocalDateTime collectionTime) {
		applicationEventPublisher.publishEvent(new SystemInfoEvent(this, makeSystemInfo(system, collectionTime)));
		applicationEventPublisher.publishEvent(new MetricCollectionEvent(this, collectionTime, metrics));
	}

	@Scheduled(fixedRateString = "1000")
	public void gather() {
		try {
			if (collection_period > 0) {
				collection_period -= 1000;
			} else {
				collection_period = properties.getRefreshSeconds() - 1000;
				enphaseImportService.collectEnphaseData().ifPresent(s -> publish(s, enphaseImportService.getMetrics(s), enphaseImportService.getCollectionTime(s)));
			}
		} catch (Exception e) {
			log.error("Failed to collect data from Enphase Controller - {}", e.getMessage(), e);
		}
	}

	private EnvoySystem makeSystemInfo(System system, LocalDateTime collectionTime) {
		return new EnvoySystem(envoyInfo.getSerialNumber(),
				envoyInfo.getSoftwareVersion(),
				Convertors.convertToLocalDateTime(system.getNetwork().getLastReportTime()),
				collectionTime,
				system.getProduction().getMicroInvertersList().size(),
				system.getNetwork().isWifi(),
				system.getNetwork().isWifi() ? system.getWireless().getCurrentNetwork().getSsid() : "LAN");
	}

}
