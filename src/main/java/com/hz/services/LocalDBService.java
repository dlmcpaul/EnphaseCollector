package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.EnvoySystemRepository;
import com.hz.interfaces.EventRepository;
import com.hz.interfaces.EventSummaryRepository;
import com.hz.interfaces.PanelRepository;
import com.hz.metrics.Metric;
import com.hz.models.database.*;
import com.hz.models.dto.PanelProduction;
import com.hz.models.events.MetricCollectionEvent;
import com.hz.models.events.SystemInfoEvent;
import com.hz.utils.Calculators;
import com.hz.utils.Convertors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class LocalDBService {

	private final EnphaseCollectorProperties properties;
	private final EnvoySystemRepository envoySystemRepository;
	private final EventRepository eventRepository;
	private final EventSummaryRepository eventSummaryRepository;
	private final PanelRepository panelRepository;

	private Event lastEvent = null;

	@EventListener
	public void systemInfoListener(SystemInfoEvent systemEvent) {
		envoySystemRepository.save(systemEvent.getEnvoySystem());
	}

	@EventListener
	public void metricListener(MetricCollectionEvent metricCollectionEvent) {
		log.debug("Writing metric stats at {} with {} items to internal database", metricCollectionEvent.getCollectionTime(), metricCollectionEvent.getMetrics().size());

		lastEvent = new Event();
		lastEvent.setTime(metricCollectionEvent.getCollectionTime());

		lastEvent.setProduction(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_PRODUCTION_CURRENT).map(Metric::getValue).orElse(BigDecimal.ZERO));
		lastEvent.setConsumption(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_CONSUMPTION_CURRENT).map(Metric::getValue).orElse(BigDecimal.ZERO));
		lastEvent.setVoltage(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_PRODUCTION_VOLTAGE).map(Metric::getValue).orElse(BigDecimal.ZERO));
		lastEvent.setPercentFull(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_BATTERY_PERCENT).map(Metric::getValue).orElse(BigDecimal.ZERO));
		lastEvent.setBatteryWatts(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_BATTERY_WATTS_REMAINING).map(Metric::getValue).orElse(BigDecimal.ZERO));
		lastEvent.setBatteryCellTemperature(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_BATTERY_MAX_CELL_TEMPERATURE).map(Metric::getValue).orElse(BigDecimal.ZERO));
		lastEvent.setGrid(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_GRID_TOTAL).map(Metric::getValue).orElse(BigDecimal.ZERO));

		int chargeState = getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_BATTERY_STATE).map(metric -> metric.getValue().intValue()).orElse(0);
		if (chargeState == 0) {
			lastEvent.setChargeState("IDLE");
			lastEvent.setBatteryPower(BigDecimal.ZERO);
			logBattery("IDLE", lastEvent, metricCollectionEvent.getMetrics());
		} else if (chargeState == -1) {
			lastEvent.setChargeState("CHARGE");
			lastEvent.setBatteryPower(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_BATTERY_POWER).map(Metric::getValue).orElse(BigDecimal.ZERO));
			logBattery("CHARGING", lastEvent, metricCollectionEvent.getMetrics());
		} else if (chargeState == 1) {
			lastEvent.setChargeState("DISCHARGE");
			lastEvent.setBatteryPower(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_BATTERY_POWER).map(Metric::getValue).orElse(BigDecimal.ZERO));
			logBattery("DISCHARGING", lastEvent, metricCollectionEvent.getMetrics());
		}

		metricCollectionEvent.getMetrics().stream().filter(Metric::isSolarPanel).forEach(lastEvent::addSolarPanel);

		eventRepository.save(lastEvent);
	}

	private void logBattery(String state, Event event, List<Metric> metrics) {
		String gridState = getMetric(metrics, Metric.METRIC_GRID_TOTAL).map(Metric::getValue).orElse(BigDecimal.ZERO).floatValue() >= 0 ? "Importing" : "Exporting";
		//log.info("BATTERY {} {}% of {} Power {} Production {} Consumption {} Grid {} {} Temp {} CellTemp {}",
		//		state,
		//		event.getPercentFull(),
		//		getMetric(metrics, Metric.METRIC_BATTERY_CAPACITY).map(Metric::getValue).orElse(BigDecimal.ZERO),
		//		event.getBatteryPower(),
		//		event.getProduction(),
		//		event.getConsumption(),
		//		gridState,
		//		event.getGrid(),
		//		getMetric(metrics, Metric.METRIC_BATTERY_MAX_TEMPERATURE).map(Metric::getValue).orElse(BigDecimal.ZERO),
		//		event.getBatteryCellTemperature()
		//);
	}

	private Optional<Metric> getMetric(List<Metric> metrics, String name) {
		return metrics.stream().filter(metric -> metric.isName(name)).findFirst();
	}

	@Transactional(readOnly = true)
	public EnvoySystem getSystemInfo() {
		return envoySystemRepository.findById(1L).orElseGet(EnvoySystem::new);
	}

	@Transactional(readOnly = true)
	public Event getLastEvent() {
		return findLastEvent();
	}

	private Event findLastEvent() {
		if (lastEvent == null) {
			return envoySystemRepository
					.findById(1L)
					.map(es -> eventRepository.findTopByTime(es.getLastReadTime()))
					.orElseGet(Event::new);
		}
		return lastEvent;
	}

	@Transactional(readOnly = true)
	public List<EventSummary> getEventsForToday() {
		return eventSummaryRepository.findEventSummariesByTimeAfter(Calculators.getMidnight());
	}

	@Transactional(readOnly = true)
	public List<Panel> getLatestPanelValue(Event lastEvent) {
		return lastEvent.getPanels();
	}

	@Transactional(readOnly = true)
	public PanelProduction getMaxPanelProduction(Event lastEvent) {
		NavigableMap<Integer, List<Panel>> map = new TreeMap<>(this.summeriseLastEvent(lastEvent));

		return map.isEmpty()
				? new PanelProduction(BigDecimal.ZERO,BigDecimal.ZERO,0)
				: new PanelProduction(BigDecimal.valueOf(map.lastEntry().getKey()), BigDecimal.ZERO, map.lastEntry().getValue().size());
	}

	@Transactional(readOnly = true)
	public Map<Integer, List<Panel>> createPanelSummaries(Event lastEvent) {
		return this.summeriseLastEvent(lastEvent);
	}

	private Map<Integer, List<Panel>> summeriseLastEvent(Event lastEvent) {
		try {
			List<Panel> panels = lastEvent.getPanels();
			return panels.stream()
					.sorted((o1, o2) -> Float.compare(o1.getPanelValue(), o2.getPanelValue()) * -1)
					.collect(Collectors.groupingBy(Panel::bucket, LinkedHashMap::new, Collectors.toList()));
		} catch (Exception e) {
			log.error("getPanelSummaries error : {}", e.getMessage(), e);
		}
		return new LinkedHashMap<>();
	}

	@Transactional(readOnly = true)
	public List<PanelSummary> getPanelProduction() {
		return panelRepository.getPanelSummaries(Calculators.getMidnight());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateCostsForToday() {
		return Calculators.calculateFinancial(eventRepository.findGridImportAfter(Calculators.getMidnight()), properties.getChargePerKiloWatt(), "Cost", properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculatePaymentForToday() {
		return Calculators.calculateFinancial(eventRepository.findGridExportAfter(Calculators.getMidnight()), properties.getPaymentPerKiloWatt(), "Payment", properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateSavingsForToday() {
		// Production + Discharged - Charged - Grid Export = Power we did not import ie savings
		LocalDateTime midnight = Calculators.getMidnight();
		Long production = eventRepository.findTotalProductionAfter(midnight);
		Long gridExport = eventRepository.findGridExportAfter(midnight);
		Long batteryDischarge = eventRepository.findDischargedAfter(midnight);
		Long batteryCharged = eventRepository.findChargedAfter(midnight);

		return Calculators.calculateFinancial( production + batteryDischarge - batteryCharged - gridExport, properties.getChargePerKiloWatt(), "Savings", properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public Long calculateMaxProduction() {
		return eventRepository.findMaxProductionAfter(Calculators.getMidnight());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateGridImport() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findGridImportAfter(Calculators.getMidnight()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateGridExport() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findGridExportAfter(Calculators.getMidnight()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateBatteryCharged() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findChargedAfter(Calculators.getMidnight()));

//		BigDecimal x = Calculators.trapezoidalIntegrationBatteryPowerNegative(eventSummaryRepository.findEventSummariesByTimeAfter(Calculators.getMidnight())).multiply(BigDecimal.valueOf(-1));
//		log.info("BatteryCharged sum {} trapezoidal {}", Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes()), Convertors.convertToKiloWattHours(x, properties.getRefreshAsMinutes()));

		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateBatteryDischarged() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findDischargedAfter(Calculators.getMidnight()));
//		BigDecimal x = Calculators.trapezoidalIntegrationBatteryPowerPositive(eventSummaryRepository.findEventSummariesByTimeAfter(Calculators.getMidnight()));
//		log.info("BatteryDischarged sum {} trapezoidal {}", Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes()), Convertors.convertToKiloWattHours(x, properties.getRefreshAsMinutes()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateTotalProduction() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findTotalProductionAfter(Calculators.getMidnight()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateTotalConsumption() {

		BigDecimal watts = BigDecimal.valueOf(eventRepository.findTotalConsumptionAfter(Calculators.getMidnight()));

//		BigDecimal x = Calculators.trapezoidalIntegrationTotalConsumptionPositive(eventSummaryRepository.findEventSummariesByTimeAfter(Calculators.getMidnight()));
//		log.info("TotalConsumption sum {} trapezoidal {}", Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes()), Convertors.convertToKiloWattHours(x, properties.getRefreshAsMinutes()));

		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}
}
