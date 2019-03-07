package com.hz.controllers;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.controllers.models.PvC;
import com.hz.controllers.models.Status;
import com.hz.models.database.EnvoySystem;
import com.hz.models.database.Event;
import com.hz.services.EnphaseService;
import com.hz.services.LocalDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by David on 23-Oct-17.
 */
@Controller
public class EnphaseController {

	private final EnphaseService enphaseService;
	private final LocalDBService localDBService;
	private final EnphaseCollectorProperties properties;

	@Autowired
	public EnphaseController(EnphaseCollectorProperties properties, EnphaseService enphaseService, LocalDBService localDBService) {
		this.enphaseService = enphaseService;
		this.properties = properties;
		this.localDBService = localDBService;
	}

	private List<Status> populateStatusList() {
		ArrayList<Status> statusList = new ArrayList<>();
		EnvoySystem envoySystem = localDBService.getSystemInfo();
		NumberFormat currency = NumberFormat.getCurrencyInstance();

		statusList.add(new Status("solar-panel.jpg","Total panels connected and sending data", String.valueOf(envoySystem.getPanelCount())));
		if (envoySystem.isWifi()) {
			statusList.add(new Status("wifi.jpg", "Home network connection", "Wifi"));
		} else {
			statusList.add(new Status("lan.jpg", "Home network connection", "LAN"));
		}
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		statusList.add(new Status("communication.png","Last communication to Enphase today", envoySystem.getLastCommunication().format(timeFormatter)));

		statusList.add(new Status("electricity.jpg", "Highest output so far today", String.valueOf(localDBService.calculateMaxProduction()) + " W"));
		statusList.add(new Status("electricity.jpg", "Paid today from exporting to grid", currency.format(localDBService.calculateTodaysPayment())));
		statusList.add(new Status("electricity.jpg", "Savings today from not using grid", currency.format(localDBService.calculateTodaysSavings())));
		statusList.add(new Status("electricity.jpg", "Cost today from grid usage", currency.format(localDBService.calculateTodaysCost())));
		statusList.add(new Status("electricity.jpg", "Daily grid access charge", currency.format(properties.getDailySupplyCharge())));

		Collections.shuffle(statusList);

		return statusList.subList(0,7);
	}

	// Generate main page from template
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("consumption", localDBService.getLastEvent().getConsumption().intValue());
		model.addAttribute("production", localDBService.getLastEvent().getProduction().intValue());
		model.addAttribute("software_version", enphaseService.getSoftwareVersion());
		model.addAttribute("serial_number", enphaseService.getSerialNumber());
		model.addAttribute("refresh_interval", properties.getRefreshSeconds());
		model.addAttribute("statusList", this.populateStatusList());
		return "index";
	}

	@GetMapping("/refreshStats")
	public String status(Model model) {
		model.addAttribute("statusList", this.populateStatusList());
		return "statsFragment :: statusPanel";
	}

	@GetMapping(value = "/event", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Event update() {
		return localDBService.getLastEvent();
	}

	@GetMapping(value = "/pvc", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public PvC getPvc() {
		PvC pvc = new PvC();

		localDBService.getTodaysEvents().stream().forEach(pvc::addEvent);

		return pvc;
	}

	@GetMapping(value = "/production", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Integer production() {
		return localDBService.getLastEvent().getProduction().intValue();
	}

	@GetMapping(value = "/consumption", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Integer consumption() {
		return localDBService.getLastEvent().getConsumption().intValue();
	}

}
