package com.hz.controllers;

import com.hz.components.ReleaseInfoContributor;
import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.controllers.models.BillAnswer;
import com.hz.controllers.models.BillQuestion;
import com.hz.controllers.models.Status;
import com.hz.models.database.EnvoySystem;
import com.hz.models.database.Summary;
import com.hz.models.dto.PanelProduction;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.*;
import com.hz.utils.Convertors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by David on 23-Oct-17.
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class TemplateController {
	private static final String DOLLAR_SIGN = "fas fa-dollar-sign";
	private static final String SOLAR_SIGN = "fas fa-sun";

	private final EnphaseCollectorProperties properties;
	private final EnvoyInfo envoyInfo;
	private final ReleaseInfoContributor release;

	private final EnvoyService envoyService;
	private final LocalDBService localDBService;

	private final SummaryService summaryService;
	private final ElectricityRateService electricityRateService;
	private final TimelineService timelineService;

	private List<Status> populateMultiStatsStatusList() {
		List<Status> statusList = new ArrayList<>();
		try {
			EnvoySystem envoySystem = localDBService.getSystemInfo();
			NumberFormat number = NumberFormat.getNumberInstance();
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

			if (envoyService.isOk()) {
				statusList.add(new Status("fas fa-rss", "Enphase data collected at", envoyService.getLastReadTime().format(timeFormatter)));
			} else {
				statusList.add(new Status("fas fa-exclamation-triangle red-icon", "Enphase data collection failed at", envoyService.getLastReadTime().format(timeFormatter)));
			}

			statusList.add(new Status("fas fa-solar-panel", "Total panels connected and sending data", String.valueOf(envoySystem.getPanelCount())));
			statusList.add(new Status("fas fa-arrow-circle-up", "Highest output so far today", localDBService.calculateMaxProduction() + " W"));
			statusList.add(new Status(SOLAR_SIGN, "Production Today", number.format(localDBService.calculateTotalProduction()) + " kWh"));
			statusList.add(new Status("fas fa-power-off", "Voltage", number.format(localDBService.getLastEvent().getVoltage()) + " V"));
			statusList.add(new Status("fas fa-broadcast-tower", "Last communication to Enphase today", envoySystem.getLastCommunication().format(timeFormatter)));
			statusList.add(new Status("fas fa-key","Authentication expires", envoyService.getExpiryAsString()));
			statusList.add(new Status(envoySystem.isWifi() ? "fas fa-wifi" : "fas fa-network-wired", "Home network", envoySystem.getNetwork()));

			if (localDBService.calculateTotalConsumption().compareTo(BigDecimal.ZERO) != 0) {   // Consumption figures available
				NumberFormat currency = NumberFormat.getCurrencyInstance();
				BigDecimal payment = localDBService.calculatePaymentForToday();
				BigDecimal cost = localDBService.calculateCostsForToday().add(BigDecimal.valueOf(properties.getDailySupplyCharge()));

				statusList.add(new Status(DOLLAR_SIGN, "Paid today from exporting to grid", currency.format(payment)));
				statusList.add(new Status(DOLLAR_SIGN, "Savings today from not using grid", currency.format(localDBService.calculateSavingsForToday())));
				statusList.add(new Status(DOLLAR_SIGN, "Cost today from grid usage", currency.format(cost)));
				statusList.add(new Status(DOLLAR_SIGN, "Cost Estimate for Today", currency.format(cost.subtract(payment))));
				statusList.add(new Status("fas fa-plug", "Consumption Today", number.format(localDBService.calculateTotalConsumption()) + " kWh"));
				statusList.add(new Status("fas fa-lightbulb", "Grid Import Today", number.format(localDBService.calculateGridImport()) + " kWh"));
			}

			PanelProduction panelProduction = localDBService.getMaxPanelProduction();
			statusList.add(new Status(SOLAR_SIGN, panelProduction.getTotalPanelsProducingMax() + " solar panels producing max ", panelProduction.getMaxProduction() + " W"));

			if (statusList.size() > 9) {
				Collections.shuffle(statusList);
			}
		} catch (Exception e) {
			log.error("populateMultiStatsStatusList Exception: {}", e.getMessage(), e);
		}
		return statusList;
	}

	private List<Status> populatePanelStatsStatusList() {
		final List<Status> statusList = new ArrayList<>();
		localDBService.createPanelSummaries().forEach((aFloat, panels) -> statusList.add(new Status(SOLAR_SIGN, panels.size() + " solar panels producing about", aFloat.intValue() + " W")));
		if (statusList.size() < 9) {
			statusList.addAll(populateMultiStatsStatusList());
		}
		return statusList;
	}

	private List<Status> populateStatusList() {
		return (ThreadLocalRandom.current().nextInt(0,2) != 0 ? populateMultiStatsStatusList() : populatePanelStatsStatusList()).subList(0,9);
	}

	// Generate main page from template
	@GetMapping("/")
	public String home(Model model, HttpServletRequest request) {
		try {
			model.addAttribute("consumption", localDBService.getLastEvent().getConsumption().intValue());
			model.addAttribute("production", localDBService.getLastEvent().getProduction().intValue());
			model.addAttribute("software_version", envoyInfo.getSoftwareVersion());
			model.addAttribute("serial_number", envoyInfo.getSerialNumber());
			model.addAttribute("software_release", envoyInfo.getReleaseDate());
			model.addAttribute("refresh_interval", properties.getRefreshSeconds());
			model.addAttribute("statusList", this.populateStatusList());
			model.addAttribute("bill_question", new BillQuestion());
			model.addAttribute("bill_answer", new BillAnswer(0));
			model.addAttribute("TZ", Calendar.getInstance().getTimeZone().toZoneId().getId());
			model.addAttribute("releaseVersion", release.getVersion());
			model.addAttribute("exportLimit", properties.getExportLimit());
			model.addAttribute("contextPath", request.getContextPath());
			model.addAttribute("timeline", timelineService.getTimeline());
		} catch (Exception e) {
			log.error("index Page Exception {}", e.getMessage(), e);
		}
		return "index";
	}

	@PostMapping("/bill")
	public String getBillAnswers(@ModelAttribute("bill_question") @Valid BillQuestion billQuestion, BindingResult bindingResult, Model model) {

		BillAnswer billAnswer = new BillAnswer(0);
		model.addAttribute("bill_answer", billAnswer);

		if (bindingResult.hasErrors()) {
			return "billQnAFragment :: billQnA(visible=false)";
		}

		billAnswer.setDaysInPeriod(billQuestion.getDateRange().getDuration());
		// Calculate Power Costs over period
		summaryService.getSummariesBetween(billQuestion.getDateRange().getFrom(), billQuestion.getDateRange().getTo())
				.forEach(total -> billAnswer.addSummary(new Summary(total.getDate(),
						Convertors.convertToKiloWattHours(total.getGridImport(), properties.getRefreshAsMinutes(total.getConversionRate())),
						Convertors.convertToKiloWattHours(total.getGridExport(), properties.getRefreshAsMinutes(total.getConversionRate())),
						Convertors.convertToKiloWattHours(total.getConsumption(), properties.getRefreshAsMinutes(total.getConversionRate())),
						Convertors.convertToKiloWattHours(total.getProduction(), properties.getRefreshAsMinutes(total.getConversionRate()))), electricityRateService.getRateForDate(total.getDate()), billQuestion));

		return "billQnAFragment :: billQnA(visible=true)";
	}

	@GetMapping("/refreshStats")
	public String status(Model model) {
		model.addAttribute("statusList", this.populateStatusList());
		return "statusListFragment :: statusList (statusList=${statusList})";
	}

}
