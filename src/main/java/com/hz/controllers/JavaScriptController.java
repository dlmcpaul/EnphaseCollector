package com.hz.controllers;

import com.hz.configuration.EnphaseCollectorProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Calendar;

@Controller
@RequiredArgsConstructor
@Log4j2
public class JavaScriptController {

	private final EnphaseCollectorProperties properties;

	@GetMapping(path = {"/initUI.js"})
	public String eventsJS(HttpServletRequest request, Model model) {
		model.addAttribute("TZ", Calendar.getInstance().getTimeZone().toZoneId().getId());
		model.addAttribute("contextPath", request.getContextPath());
		model.addAttribute("exportLimit", properties.getExportLimit());
		model.addAttribute("refreshInterval", properties.getRefreshSeconds());
		model.addAttribute("refreshBarInterval", properties.getRefreshSeconds() / 1000);

		return "initUI.js";
	}

}
