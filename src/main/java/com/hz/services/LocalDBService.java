package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.EnvoySystemRepository;
import com.hz.interfaces.EventRepository;
import com.hz.interfaces.LocalExportInterface;
import com.hz.metrics.Metric;
import com.hz.models.database.EnvoySystem;
import com.hz.models.database.Event;
import com.hz.models.database.Panel;
import com.hz.utils.Convertors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LocalDBService implements LocalExportInterface {

	private static final Logger LOG = LoggerFactory.getLogger(LocalDBService.class);

	private final EnvoySystemRepository envoySystemRepository;
	private final EventRepository eventRepository;
	private final EnphaseCollectorProperties properties;

	@Autowired
	public LocalDBService(EnphaseCollectorProperties properties, EnvoySystemRepository envoySystemRepository, EventRepository eventRepository) {
		this.properties = properties;
		this.envoySystemRepository = envoySystemRepository;
		this.eventRepository = eventRepository;
	}

	@Override
	public void sendMetrics(List<Metric> metrics, Date readTime) {
		LOG.debug("Writing stats at {} with {} items", readTime, metrics.size());

		Event event = new Event();
		event.setTime((LocalDateTime.ofInstant(readTime.toInstant(), ZoneId.systemDefault())));

		event.setProduction(getMetric(metrics, "solar.production.current").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));
		event.setConsumption(getMetric(metrics, "solar.consumption.current").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO));

		metrics.forEach(m -> event.addSolarPanel(new Panel(m.getName(), m.getValue())));

		eventRepository.save(event);
	}

	private Optional<Metric> getMetric(List<Metric> metrics, String name) {
		return metrics.stream().filter(metric -> metric.getName().equalsIgnoreCase(name)).findFirst();
	}

	@Override
	public void sendSystemInfo(EnvoySystem envoySystem) {
		envoySystemRepository.save(envoySystem);
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

	public BigDecimal calculateTodaysCost() {
		return calculateFinancial(eventRepository.findExcessConsumptionAfter(getMidnight()), properties.getChargePerKiloWatt(), "Cost");
	}

	public BigDecimal calculateTodaysPayment() {
		return calculateFinancial(eventRepository.findExcessProductionAfter(getMidnight()), properties.getPaymentPerKiloWatt(), "Payment");
	}

	public BigDecimal calculateTodaysSavings() {
		Long totalWatts = eventRepository.findTotalProductionAfter(getMidnight());
		Long excessWatts = eventRepository.findExcessProductionAfter(getMidnight());
		return calculateFinancial( totalWatts - excessWatts , properties.getChargePerKiloWatt(), "Savings");
	}

	public Long calculateMaxProduction() {
		return eventRepository.findMaxProductionAfter(getMidnight());
	}

	private BigDecimal calculateFinancial(Long recordedWatts, double price, String type) {

		BigDecimal watts = BigDecimal.ZERO;
		final NumberFormat numberInstance = NumberFormat.getNumberInstance();
		final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();

		if (recordedWatts > 0) {
			watts = BigDecimal.valueOf(recordedWatts);
		}

		BigDecimal wattHours = Convertors.convertToWattHours(watts, properties.getRefreshSeconds());

		// Convert to KWh = Wh / 1000
		BigDecimal kiloWattHours = wattHours.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
		// Convert to dollars cost = KWh * price per kilowatt
		BigDecimal moneyValue = kiloWattHours.multiply(BigDecimal.valueOf(price));

		// NOSONAR
		LOG.debug("{} - {} calculated from {} Kwh using {} per Kwh and input of {} W ", type, currencyInstance.format(moneyValue), numberInstance.format(kiloWattHours), price, watts);

		return moneyValue;
	}

	private LocalDateTime getMidnight() {
		LocalDateTime now = LocalDateTime.now();
		return now.toLocalDate().atStartOfDay();
	}

}
