package com.hz.controllers;

import com.hz.components.ReleaseInfoContributor;
import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.controllers.models.*;
import com.hz.models.database.EnvoySystem;
import com.hz.models.database.Event;
import com.hz.models.database.Summary;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnphaseService;
import com.hz.services.LocalDBService;
import com.hz.utils.Convertors;
import com.hz.utils.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by David on 23-Oct-17.
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class EnphaseController {
	private static final String DOLLAR_SIGN = "fas fa-dollar-sign";

	private final EnphaseService enphaseService;
	private final LocalDBService localDBService;
	private final EnphaseCollectorProperties properties;
	private final EnvoyInfo envoyInfo;
	private final ReleaseInfoContributor release;

	private List<Status> populateStatusList() {
		ArrayList<Status> statusList = new ArrayList<>();
		try {
			EnvoySystem envoySystem = localDBService.getSystemInfo();
			NumberFormat currency = NumberFormat.getCurrencyInstance();
			NumberFormat number = NumberFormat.getNumberInstance();
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			BigDecimal payment = localDBService.calculateTodaysPayment();
			BigDecimal cost = localDBService.calculateTodaysCost().add(BigDecimal.valueOf(properties.getDailySupplyCharge()));

			statusList.add(new Status("fas fa-solar-panel", "Total panels connected and sending data", String.valueOf(envoySystem.getPanelCount())));
			statusList.add(new Status(envoySystem.isWifi() ? "fas fa-wifi" : "fas fa-network-wired", "Home network", envoySystem.getNetwork()));

			statusList.add(new Status("fas fa-broadcast-tower", "Last communication to Enphase today", envoySystem.getLastCommunication().format(timeFormatter)));

			statusList.add(new Status("fas fa-arrow-circle-up", "Highest output so far today", localDBService.calculateMaxProduction() + " W"));
			statusList.add(new Status(DOLLAR_SIGN, "Paid today from exporting to grid", currency.format(payment)));
			statusList.add(new Status(DOLLAR_SIGN, "Savings today from not using grid", currency.format(localDBService.calculateTodaysSavings())));
			statusList.add(new Status(DOLLAR_SIGN, "Cost today from grid usage", currency.format(cost)));
			statusList.add(new Status(DOLLAR_SIGN, "Cost Estimate for Today", currency.format(cost.subtract(payment))));

			statusList.add(new Status("fas fa-sun", "Production Today", number.format(localDBService.calculateTotalProduction()) + " kWh"));
			statusList.add(new Status("fas fa-plug", "Consumption Today", number.format(localDBService.calculateTotalConsumption()) + " kWh"));
			statusList.add(new Status("fas fa-lightbulb", "Grid Import Today", number.format(localDBService.calculateGridImport()) + " kWh"));
			statusList.add(new Status("fas fa-power-off", "Voltage", number.format(localDBService.getLastEvent().getVoltage()) + " V"));
			if (enphaseService.isOk()) {
				statusList.add(new Status("fas fa-rss", "Enphase data collected at", enphaseService.getLastReadTime().format(timeFormatter)));
			} else {
				statusList.add(new Status("fas fa-exclamation-triangle red-icon", "Enphase data collection failed at", enphaseService.getLastReadTime().format(timeFormatter)));
			}

			Event event = localDBService.getLastEvent();
			BigDecimal max = event.getMaxPanelProduction();
			statusList.add(new Status("fas fa-sun", event.countMaxPanelsProducing(max) + " solar panels producing max ", max + " W"));

			Collections.shuffle(statusList);

			return statusList.subList(0, 9);
		} catch (Exception e) {
			log.error("populateStatusList Exception: {} {}", e.getMessage(), e);
		}

		return statusList;
	}

	// Generate main page from template
	@GetMapping("/")
	public String home(Model model) {
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
		} catch (Exception e) {
			log.error("index Page Exception {} {}", e.getMessage(), e);
		}
		return "index";
	}

	@PostMapping("/bill")
	public String getBillAnswers(@ModelAttribute("bill_question") BillQuestion billQuestion, Model model) {
		BillAnswer billAnswer = new BillAnswer(billQuestion.getDateRange().daysInPeriod());

		// Calculate Power Costs over period
		localDBService.getSummaries(billQuestion.getDateRange().getFromDate(), billQuestion.getDateRange().getToDate())
				.forEach(total -> billAnswer.addSummary(new Summary(total.getDate(),
						Convertors.convertToKiloWattHours(total.getGridImport(), properties.getRefreshAsMinutes()),
						Convertors.convertToKiloWattHours(total.getGridExport(), properties.getRefreshAsMinutes()),
						Convertors.convertToKiloWattHours(total.getConsumption(), properties.getRefreshAsMinutes()),
						Convertors.convertToKiloWattHours(total.getProduction(), properties.getRefreshAsMinutes())), localDBService.getRateForDate(total.getDate()), billQuestion));

		model.addAttribute("bill_answer", billAnswer);
		return "billAnswerFragment :: billAnswer(visible=true)";
	}

	@GetMapping("/refreshStats")
	public String status(Model model) {
		model.addAttribute("statusList", this.populateStatusList());
		return "statusListFragment :: statusList";
	}

	@GetMapping(value = "/event", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Event getEvent() {
		try {
			return localDBService.getLastEvent();
		} catch (Exception e) {
			log.error("getEvent Exception: {} {}", e.getMessage(), e);
		}

		return new Event();
	}

	@GetMapping(value = "/pvc", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public PvC getPvc() {
		PvC pvc = new PvC();

		try {
			localDBService.getTodaysEvents().forEach(pvc::addEvent);
		} catch (Exception e) {
			log.error("getPvc Exception: {} {}", e.getMessage(), e);
		}
		return pvc;
	}

	@GetMapping(value = "/history", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public History getHistory(@RequestParam String duration) {
		History result = new History();

		if (Validators.isValidDuration(duration)) {
			try {
				localDBService.getLastDurationTotalsContinuous(duration)
						.forEach(total -> result.addSummary(new Summary(total.getDate(),
								Convertors.convertToKiloWattHours(total.getGridImport(), properties.getRefreshAsMinutes()),
								Convertors.convertToKiloWattHours(total.getGridExport(), properties.getRefreshAsMinutes()),
								Convertors.convertToKiloWattHours(total.getConsumption(), properties.getRefreshAsMinutes()),
								Convertors.convertToKiloWattHours(total.getProduction(), properties.getRefreshAsMinutes())), localDBService.getRateForDate(total.getDate()), duration));
			} catch (Exception e) {
				log.error("getHistory Exception: {} {}", e.getMessage(), e);
			}
		}
		return result;
	}

	@GetMapping(value = "/production", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Integer getProduction() {
		try {
			return localDBService.getLastEvent().getProduction().intValue();
		} catch (Exception e) {
			log.error("getProduction Exception: {} {}", e.getMessage(), e);
		}
		return 0;
	}

	@GetMapping(value = "/consumption", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Integer getConsumption() {
		try {
			return localDBService.getLastEvent().getConsumption().intValue();
		} catch (Exception e) {
			log.error("getConsumption Exception: {} {}", e.getMessage(), e);
		}
		return 0;
	}

}
