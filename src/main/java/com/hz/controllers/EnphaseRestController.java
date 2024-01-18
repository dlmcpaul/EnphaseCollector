package com.hz.controllers;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.controllers.models.History;
import com.hz.controllers.models.PlotBand;
import com.hz.controllers.models.PvC;
import com.hz.models.database.Summary;
import com.hz.services.LocalDBService;
import com.hz.utils.Convertors;
import com.hz.utils.Validators;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by David on 23-Oct-17.
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class EnphaseRestController {

	private final LocalDBService localDBService;
	private final EnphaseCollectorProperties properties;

	@GetMapping(value = "/pvc", produces = "application/json; charset=UTF-8")
	public PvC getPvc() {
		PvC pvc = new PvC();

		try {
			localDBService.getEventsForToday().forEach(pvc::addEvent);
			pvc.generateExcess(localDBService.getPanelProduction(), properties.getExportLimit());
			pvc.setPlotBands(properties.getBands().stream().map(b -> new PlotBand(b.getFrom(), b.getTo(), b.getColour())).toList());
		} catch (Exception e) {
			log.error("getPvc Exception: {}", e.getMessage(), e);
		}
		return pvc;
	}

	@GetMapping(value = "/history", produces = "application/json; charset=UTF-8")
	public History getHistory(@Valid @RequestParam @NotNull String duration) {
		History result = new History();

		if (Validators.isValidDuration(duration)) {
			try {
				localDBService.getLastDurationTotalsContinuous(duration)
						.forEach(total -> result.addSummary(new Summary(total.getDate(),
								Convertors.convertToKiloWattHours(total.getGridImport(), properties.getRefreshAsMinutes(total.getConversionRate())),
								Convertors.convertToKiloWattHours(total.getGridExport(), properties.getRefreshAsMinutes(total.getConversionRate())),
								Convertors.convertToKiloWattHours(total.getConsumption(), properties.getRefreshAsMinutes(total.getConversionRate())),
								Convertors.convertToKiloWattHours(total.getProduction(), properties.getRefreshAsMinutes(total.getConversionRate()))), localDBService.getRateForDate(total.getDate()), duration));
			} catch (Exception e) {
				log.error("getHistory Exception: {}", e.getMessage(), e);
			}
		}
		return result;
	}

	@GetMapping(value = "/production", produces = "application/json; charset=UTF-8")
	public Integer getProduction() {
		try {
			return localDBService.getLastEvent().getProduction().intValue();
		} catch (Exception e) {
			log.error("getProduction Exception: {}", e.getMessage(), e);
		}
		return 0;
	}

	@GetMapping(value = "/consumption", produces = "application/json; charset=UTF-8")
	public Integer getConsumption() {
		try {
			return localDBService.getLastEvent().getConsumption().intValue();
		} catch (Exception e) {
			log.error("getConsumption Exception: {}", e.getMessage(), e);
		}
		return 0;
	}

}
