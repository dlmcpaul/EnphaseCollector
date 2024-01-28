package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.controllers.models.Timeline;
import com.hz.controllers.models.TimelineEntry;
import com.hz.models.database.Summary;
import com.hz.utils.Calculators;
import com.hz.utils.Convertors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class TimelineService {

	private final EnphaseCollectorProperties properties;
	private final SummaryService summaryService;

	@Transactional(readOnly = true)
	public Timeline getTimeline() {
		Summary firstEntry = summaryService.findFirstSummary();
		if (firstEntry == null) {
			return new Timeline();
		}

		LocalDate earliestEntry = firstEntry.getDate();
		int year = earliestEntry.getYear();
		Timeline timeline = new Timeline(earliestEntry);

		List<Summary> summaries = summaryService.getSummariesBetween(earliestEntry, Calculators.getMidnight().toLocalDate());

		Summary highestOutput = new Summary();
		Summary highestProduction = new Summary();
		Summary highestGridImport = new Summary();
		Summary highestGridExport = new Summary();

		for (var summary : summaries) {
			if (summary.getDate().getYear() > year) {
				summarise(timeline, highestOutput, highestProduction, highestGridImport, highestGridExport);

				highestOutput = new Summary();
				highestProduction = new Summary();
				highestGridImport = new Summary();
				highestGridExport = new Summary();
				year = summary.getDate().getYear();
			}

			if (summary.getHighestOutput() != null && (summary.getHighestOutput().compareTo(highestOutput.getHighestOutput()) >= 0)) {
				highestOutput = summary;
			}
			if (summary.getProduction().compareTo(highestProduction.getProduction()) >= 0) {
				highestProduction = summary;
			}
			if (summary.getGridImport().compareTo(highestGridImport.getGridImport()) >= 0) {
				highestGridImport = summary;
			}
			if (summary.getGridExport().compareTo(highestGridExport.getGridExport()) >= 0) {
				highestGridExport = summary;
			}

		}
		summarise(timeline, highestOutput, highestProduction, highestGridImport, highestGridExport);

		return timeline;
	}

	private void summarise(Timeline timeline, Summary highestOutput, Summary highestProduction, Summary highestGridImport, Summary highestGridExport) {
		if (highestOutput.getHighestOutput() != 0L) {
			timeline.addTimeLine(highestOutput.getDate(), TimelineEntry.EntryType.HIGHEST_POWER_ACHIEVED, BigDecimal.valueOf(highestOutput.getHighestOutput()));
		}
		timeline.addTimeLine(highestProduction.getDate(), TimelineEntry.EntryType.HIGHEST_PRODUCTION, Convertors.convertToKiloWattHours(highestProduction.getProduction(), properties.getRefreshAsMinutes(highestProduction.getConversionRate())));
		timeline.addTimeLine(highestGridImport.getDate(), TimelineEntry.EntryType.HIGHEST_GRID_IMPORT, Convertors.convertToKiloWattHours(highestGridImport.getGridImport(), properties.getRefreshAsMinutes(highestGridImport.getConversionRate())));
		timeline.addTimeLine(highestGridExport.getDate(), TimelineEntry.EntryType.HIGHEST_SOLAR_EXPORT, Convertors.convertToKiloWattHours(highestGridExport.getGridExport(), properties.getRefreshAsMinutes(highestGridExport.getConversionRate())));
	}

}
