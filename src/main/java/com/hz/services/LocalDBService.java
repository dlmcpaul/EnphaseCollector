package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.EnvoySystemRepository;
import com.hz.interfaces.EventRepository;
import com.hz.interfaces.LocalExportInterface;
import com.hz.interfaces.SummaryRepository;
import com.hz.metrics.Metric;
import com.hz.models.database.*;
import com.hz.utils.Calculators;
import com.hz.utils.Convertors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LocalDBService implements LocalExportInterface {

	private static final Logger LOG = LoggerFactory.getLogger(LocalDBService.class);

	private final EnvoySystemRepository envoySystemRepository;
	private final EventRepository eventRepository;
	private final SummaryRepository summaryRepository;
	private final EnphaseCollectorProperties properties;

	@Autowired
	public LocalDBService(EnphaseCollectorProperties properties, EnvoySystemRepository envoySystemRepository, EventRepository eventRepository, SummaryRepository summaryRepository) {
		this.properties = properties;
		this.envoySystemRepository = envoySystemRepository;
		this.eventRepository = eventRepository;
		this.summaryRepository = summaryRepository;
	}

	@Override
	public void sendMetrics(List<Metric> metrics, LocalDateTime readTime) {
		LOG.debug("Writing stats at {} with {} items", readTime, metrics.size());

		Event event = new Event();
		event.setTime(readTime);

		event.setProduction(getMetric(metrics, "solar.production.current").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));
		event.setConsumption(getMetric(metrics, "solar.consumption.current").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));
		event.setVoltage(getMetric(metrics, "solar.production.voltage").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));

		metrics.forEach(m -> event.addSolarPanel(new Panel(m.getName(), m.getValue())));

		eventRepository.save(event);
	}

	@Override
	public void sendSystemInfo(EnvoySystem envoySystem) {
		envoySystemRepository.save(envoySystem);
	}

	/*
	Summarise each day into production, consumption, grid import and grid export
	 */
	@Transactional
	public void summariseEvents() {
		List<DailySummary> dailies = eventRepository.findAllBefore(getMidnight());
		List<Total> gridImports = eventRepository.findAllExcessConsumptionBefore(getMidnight());
		List<Total> gridExports = eventRepository.findAllExcessProductionBefore(getMidnight());

		LOG.info("Storing {} summary records", dailies.size());
		dailies.stream().forEach(daily -> gridImports.stream().
			filter(gridImport -> daily.getDate().isEqual(gridImport.getDate())).
			findFirst().
			ifPresent(gridImportMatch -> gridExports.stream().
				filter(gridExport -> daily.getDate().isEqual(gridExport.getDate())).
				findFirst().
				ifPresent(gridExportMatch -> summaryRepository.save(new Summary(daily, gridImportMatch, gridExportMatch)))));

		eventRepository.deleteEventsByTimeBefore(getMidnight());
	}

	private Optional<Metric> getMetric(List<Metric> metrics, String name) {
		return metrics.stream().filter(metric -> metric.getName().equalsIgnoreCase(name)).findFirst();
	}

	public EnvoySystem getSystemInfo() {
		return envoySystemRepository.findById(1L).orElseGet(EnvoySystem::new);
	}

	public Event getLastEvent() {
		Optional<EnvoySystem> envoySystem = envoySystemRepository.findById(1L);

		return envoySystem.map(es -> eventRepository.findTopByTime(es.getLastReadTime())).orElseGet(Event::new);
	}

	public List<Event> getTodaysEvents() {
		return eventRepository.findEventsByTimeAfter(getMidnight());
	}

	public List<Summary> getLastDurationTotals(String duration) {
		return summaryRepository.findSummeriesByDateGreaterThanEqual(getFromDuration(duration));
	}

	public BigDecimal calculateTodaysCost() {
		return Calculators.calculateFinancial(eventRepository.findExcessConsumptionAfter(getMidnight()), properties.getChargePerKiloWatt(), "Cost", properties.getRefreshAsMinutes());
	}

	public BigDecimal calculateTodaysPayment() {
		return Calculators.calculateFinancial(eventRepository.findExcessProductionAfter(getMidnight()), properties.getPaymentPerKiloWatt(), "Payment", properties.getRefreshAsMinutes());
	}

	public BigDecimal calculateTodaysSavings() {
		Long totalWatts = eventRepository.findTotalProductionAfter(getMidnight());
		Long excessWatts = eventRepository.findExcessProductionAfter(getMidnight());
		return Calculators.calculateFinancial( totalWatts - excessWatts , properties.getChargePerKiloWatt(), "Savings", properties.getRefreshAsMinutes());
	}

	public Long calculateMaxProduction() {
		return eventRepository.findMaxProductionAfter(getMidnight());
	}

	public BigDecimal calculateGridImport() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findExcessConsumptionAfter(getMidnight()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	public BigDecimal calculateTotalProduction() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findTotalProductionAfter(getMidnight()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	public BigDecimal calculateTotalConsumption() {
		BigDecimal watts = BigDecimal.valueOf(eventRepository.findTotalConsumptionAfter(getMidnight()));
		return Convertors.convertToKiloWattHours(watts, properties.getRefreshAsMinutes());
	}

	private LocalDateTime getMidnight() {
		LocalDate now = LocalDate.now();
		return now.atStartOfDay();
	}

	private LocalDate getFromDuration(String duration) {
		LocalDate now = LocalDate.now();
		return now.plus(Integer.valueOf(duration.substring(0,1)) * -1L, ChronoUnit.valueOf(duration.substring(1).toUpperCase()));
	}
}
