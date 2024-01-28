package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.ElectricityRateRepository;
import com.hz.models.database.ElectricityRate;
import com.hz.models.database.Summary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ElectricityRateService {

	private final EnphaseCollectorProperties properties;
	private final SummaryService summaryService;
	private final ElectricityRateRepository electricityRateRepository;

	// TODO only detects charge per kilowatt changes
	@Transactional
	public void upgradeRates() {
		Optional<ElectricityRate> rate = this.findRateFor(LocalDate.now());
		if (rate.isEmpty()) {
			// First creation set effective to first summary event
			log.info("Upgrading Rate Table (First Run)");
			this.saveElectricityRate(new ElectricityRate(properties.getPaymentPerKiloWatt(), properties.getChargePerKiloWatt(), properties.getDailySupplyCharge()));
		} else if (properties.getEffectiveRateDate() == null && rate.get().getChargePerKiloWatt().compareTo(properties.getChargePerKiloWatt()) != 0) {
			// Rate has changes set new rate from today
			log.info("Upgrading Rate Table (New Rate Set as at Today)");
			this.saveElectricityRate(LocalDate.now(), new ElectricityRate(properties.getPaymentPerKiloWatt(), properties.getChargePerKiloWatt(), properties.getDailySupplyCharge()));
		} else if (properties.getEffectiveRateDate() != null && properties.getEffectiveRateDate().isAfter(rate.get().getEffectiveDate())) {
			// Rate is changing from new date
			log.info("Upgrading Rate Table (New Rate set effective {})", properties.getEffectiveRateDate());
			this.saveElectricityRate(properties.getEffectiveRateDate(), new ElectricityRate(properties.getPaymentPerKiloWatt(), properties.getChargePerKiloWatt(), properties.getDailySupplyCharge()));
		}
	}

	@Transactional(readOnly = true)
	public ElectricityRate getRateForDate(LocalDate date) {
		return this.findRateFor(date).orElse(new ElectricityRate(date));
	}

	private Optional<ElectricityRate> findRateFor(LocalDate date) {
		return electricityRateRepository.findFirstByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(date);
	}

	private void saveElectricityRate(LocalDate effectiveDate, ElectricityRate electricityRate) {
		log.info("Storing new Rate effective {}", effectiveDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
		electricityRate.setEffectiveDate(effectiveDate);
		electricityRateRepository.save(electricityRate);
	}

	private void saveElectricityRate(ElectricityRate electricityRate) {
		Summary summary = summaryService.findFirstSummary();
		if (summary == null) {
			// No Summaries yet
			this.saveElectricityRate(LocalDate.now(), electricityRate);
		} else {
			this.saveElectricityRate(summary.getDate(), electricityRate);
		}
	}

}
