package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.EventRepository;
import com.hz.interfaces.PanelRepository;
import com.hz.interfaces.SummaryRepository;
import com.hz.models.database.DailySummary;
import com.hz.models.database.EmptyTotal;
import com.hz.models.database.Summary;
import com.hz.models.database.Total;
import com.hz.utils.Calculators;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SummaryService {

	private final EnphaseCollectorProperties properties;
	private final EventRepository eventRepository;
	private final PanelRepository panelRepository;
	private final SummaryRepository summaryRepository;

	// Summarise the Event table at 5 minutes past midnight
	@Scheduled(cron="0 5 0 * * ?")
	@Transactional
	public void createSummaries() {
		LocalDateTime midnight = Calculators.getMidnight();
		log.info("Summarising Event table prior to {}", midnight);
		try {
			List<DailySummary> dailies = eventRepository.findAllBefore(midnight);
			List<Total> gridImports = eventRepository.findAllExcessConsumptionBefore(midnight);
			List<Total> gridExports = eventRepository.findAllExcessProductionBefore(midnight);
			List<Total> maxProductions = eventRepository.findAllMaxProductionBefore(midnight);

			dailies.forEach(daily -> saveSummary(daily,
					findMatching(gridImports, daily.getDate()),
					findMatching(gridExports, daily.getDate()),
					findMatching(maxProductions, daily.getDate()), properties.getRefreshAsMinutes()));

			panelRepository.deleteEventsPanelByTimeBefore(midnight);
			panelRepository.deletePanelsByTimeBefore(midnight);
			eventRepository.deleteEventsByTimeBefore(midnight);
		} catch (Exception e) {
			log.error("Failed to summarise Event table: {} {}", e.getMessage(), e);
		}
	}
	@Transactional
	public void upgradeConversion() {
		summaryRepository.updateAllSummariesWithConversion(properties.getRefreshAsMinutes());
	}

	@Transactional(readOnly=true)
	public Summary findFirstSummary() {
		return summaryRepository.findFirst();
	}

	@Transactional(readOnly = true)
	public List<Summary> getSummariesBetween(LocalDate from, LocalDate to) {
		return this.getSummaries(from, to);
	}

	// When a summary record is null the list is not continuous so fill missing values
	@Transactional(readOnly = true)
	public List<Summary> getLastDurationTotalsContinuous(String duration) {
		List<Summary> dbValues = this.getSummaries(Calculators.calculateStartDateFromDuration(duration), Calculators.calculateEndDateFromDuration(duration));
		List<Summary> result = new ArrayList<>();

		LocalDate dateIndex = Calculators.calculateStartDateFromDuration(duration);
		int dbIndex = 0;

		while (dbIndex < dbValues.size()) {
			if (dateIndex.isEqual(dbValues.get(dbIndex).getDate())) {
				result.add(dbValues.get(dbIndex++));
				dateIndex = dateIndex.plusDays(1);
			} else if (dateIndex.isBefore(dbValues.get(dbIndex).getDate())) {
				result.add(new Summary(dateIndex));
				dateIndex = dateIndex.plusDays(1);
			} else {
				result.add(dbValues.get(dbIndex++));
			}
		}

		return result;
	}

	private List<Summary> getSummaries(LocalDate from, LocalDate to) {
		List<Summary> summaries = summaryRepository.findSummariesByDateBetweenOrderByDateAsc(from, to);
		log.debug("Days returned {}", summaries.size());
		return summaries;
	}

	private void saveSummary(DailySummary daily, Total gridImport, Total gridExport, Total highestOutput, BigDecimal conversionRate) {
		log.info("Saving Summary for {} with import {} and export {}", daily.getDate(), gridImport.getSummary(), gridExport.getSummary());
		summaryRepository.save(new Summary(daily, gridImport, gridExport, highestOutput, conversionRate));
	}

	private Total findMatching(List<Total> values, LocalDate matchDate) {
		return values.stream().
				filter(value -> matchDate.isEqual(value.getDate())).
				findFirst().
				orElseGet(EmptyTotal::new);
	}

}
