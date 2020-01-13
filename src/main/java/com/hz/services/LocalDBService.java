package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.*;
import com.hz.metrics.Metric;
import com.hz.models.database.*;
import com.hz.utils.Calculators;
import com.hz.utils.Convertors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class LocalDBService implements LocalExportInterface {

	private final EnphaseCollectorProperties properties;
	private final EnvoySystemRepository envoySystemRepository;
	private final EventRepository eventRepository;
	private final SummaryRepository summaryRepository;
	private final ElectricityRateRepository electricityRateRepository;

	@Override
	public void sendMetrics(List<Metric> metrics, LocalDateTime readTime) {
		log.debug("Writing stats at {} with {} items", readTime, metrics.size());

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
		List<Total> maxProduction = eventRepository.findAllMaxProductionBefore(getMidnight());

		log.info("Storing {} summary records", dailies.size());
		dailies.forEach(daily -> findMatching(maxProduction, daily.getDate()).
			ifPresent(maxProductionMatch -> findMatching(gridImports, daily.getDate()).
			ifPresent(gridImportMatch -> findMatching(gridExports, daily.getDate()).
			ifPresent(gridExportMatch -> saveSummary(daily, gridImportMatch, gridExportMatch, maxProductionMatch)))));

		eventRepository.deleteEventsByTimeBefore(getMidnight());
	}

	private void saveSummary(DailySummary daily, Total gridImport, Total gridExport, Total highestOutput) {
		summaryRepository.save(new Summary(daily, gridImport, gridExport, highestOutput));
	}

	public void saveElectricityRate(LocalDate effectiveDate, ElectricityRate electricityRate) {
		electricityRate.setEffectiveDate(effectiveDate);
		electricityRateRepository.save(electricityRate);
	}

	public void saveElectricityRate(ElectricityRate electricityRate) {
		Summary summary = summaryRepository.findFirst();
		this.saveElectricityRate(summary.getDate(), electricityRate);
	}

	private Optional<Total> findMatching(List<Total> values, LocalDate matchDate) {
		return values.stream().filter(value -> matchDate.isEqual(value.getDate())).findFirst();
	}

	public ElectricityRate getRateForDate(LocalDate date) {
		return electricityRateRepository.findFirstByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(date);
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
		return this.getSummaries(calculateFromDateDuration(duration), calculateToDateDuration(duration));
	}

	public List<Summary> getSummaries(LocalDate from, LocalDate to) {
		List<Summary> summaries = summaryRepository.findSummeriesByDateBetween(from, to);
		log.info("Days returned {}", summaries.size());
		return summaries;
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

	private LocalDate calculateFromDateDuration(String duration) {
		LocalDate base = LocalDate.now();

		int amount = Integer.parseInt(duration.substring(0,1));
		String unit = duration.substring(1).toUpperCase();

		if (unit.equalsIgnoreCase("WEEKS") && base.getDayOfWeek().equals(DayOfWeek.SUNDAY) == false) {
			base = base.minusDays(base.getDayOfWeek().getValue());
		}
		if (unit.equalsIgnoreCase("MONTHS")) {
			base = base.minusDays(base.getDayOfMonth()).plusDays(1);
		}
		return base.plus(amount * -1L, ChronoUnit.valueOf(unit));
	}

	private LocalDate calculateToDateDuration(String duration) {
		LocalDate base = LocalDate.now();
		String unit = duration.substring(1).toUpperCase();

		// We want SUN to SAT as a WEEK
		if (unit.equalsIgnoreCase("WEEKS") && base.getDayOfWeek().equals(DayOfWeek.SUNDAY) == false) {
			base = base.minusDays(base.getDayOfWeek().getValue());
		}
		if (unit.equalsIgnoreCase("MONTHS")) {
			return base.minusDays(base.getDayOfMonth());
		}
		return base.minusDays(1);
	}

}
