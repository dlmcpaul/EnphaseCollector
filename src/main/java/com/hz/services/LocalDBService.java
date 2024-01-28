package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.EnvoySystemRepository;
import com.hz.interfaces.EventRepository;
import com.hz.interfaces.PanelRepository;
import com.hz.metrics.Metric;
import com.hz.models.database.EnvoySystem;
import com.hz.models.database.Event;
import com.hz.models.database.Panel;
import com.hz.models.database.PanelSummary;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class LocalDBService {

	private final EnphaseCollectorProperties properties;
	private final EnvoySystemRepository envoySystemRepository;
	private final EventRepository eventRepository;
	private final PanelRepository panelRepository;

	@EventListener
	public void systemInfoListener(SystemInfoEvent systemEvent) {
		envoySystemRepository.save(systemEvent.getEnvoySystem());
	}

	@EventListener
	public void metricListener(MetricCollectionEvent metricCollectionEvent) {
		log.debug("Writing metric stats at {} with {} items to internal database", metricCollectionEvent.getCollectionTime(), metricCollectionEvent.getMetrics().size());

		Event event = new Event();
		event.setTime(metricCollectionEvent.getCollectionTime());

		event.setProduction(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_PRODUCTION_CURRENT).map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));
		event.setConsumption(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_CONSUMPTION_CURRENT).map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));
		event.setVoltage(getMetric(metricCollectionEvent.getMetrics(), Metric.METRIC_PRODUCTION_VOLTAGE).map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));

		metricCollectionEvent.getMetrics().stream().filter(Metric::isSolarPanel).forEach(event::addSolarPanel);

		eventRepository.save(event);
	}

	private Optional<Metric> getMetric(List<Metric> metrics, String name) {
		return metrics.stream().filter(metric -> metric.getName().equalsIgnoreCase(name)).findFirst();
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
		return envoySystemRepository
				.findById(1L)
				.map(es -> eventRepository.findTopByTime(es.getLastReadTime()))
				.orElseGet(Event::new);
	}

	@Transactional(readOnly = true)
	public List<Event> getEventsForToday() {
		return eventRepository.findEventsByTimeAfter(Calculators.getMidnight());
	}

	@Transactional(readOnly = true)
	public PanelProduction getMaxPanelProduction() {
		NavigableMap<Float,List<Panel>> map = new TreeMap<>(this.summeriseLastEvent());

		return map.isEmpty()
				? new PanelProduction(BigDecimal.ZERO,BigDecimal.ZERO,0)
				: new PanelProduction(BigDecimal.valueOf(map.lastEntry().getKey()), BigDecimal.ZERO, map.lastEntry().getValue().size());
	}

	@Transactional(readOnly = true)
	public Map<Float, List<Panel>> createPanelSummaries() {
		return this.summeriseLastEvent();
	}

	private Map<Float, List<Panel>> summeriseLastEvent() {
		try {
			List<Panel> panels = this.findLastEvent().getPanels();
			panels.sort((o1, o2) -> Float.compare(o1.getPanelValue(), o2.getPanelValue()) * -1);
			return panels.stream()
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
		return Calculators.calculateFinancial(eventRepository.findExcessConsumptionAfter(Calculators.getMidnight()), properties.getChargePerKiloWatt(), "Cost", properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculatePaymentForToday() {
		return Calculators.calculateFinancial(eventRepository.findExcessProductionAfter(Calculators.getMidnight()), properties.getPaymentPerKiloWatt(), "Payment", properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateSavingsForToday() {
		Long totalWatts = eventRepository.findTotalProductionAfter(Calculators.getMidnight());
		Long excessWatts = eventRepository.findExcessProductionAfter(Calculators.getMidnight());
		return Calculators.calculateFinancial( totalWatts - excessWatts , properties.getChargePerKiloWatt(), "Savings", properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly = true)
	public Long calculateMaxProduction() {
		return eventRepository.findMaxProductionAfter(Calculators.getMidnight());
	}

	@Transactional(readOnly = true)
	public BigDecimal calculateGridImport() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findExcessConsumptionAfter(Calculators.getMidnight()));
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
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}
}
