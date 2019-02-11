package com.hz.controllers;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.controllers.models.SystemInfo;
import com.hz.metrics.Metric;
import com.hz.services.OutputManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

/**
 * Created by David on 23-Oct-17.
 */
@Controller
public class EnphaseController {

	private final OutputManager outputManager;
	private final EnphaseCollectorProperties properties;

	@Autowired
	public EnphaseController(OutputManager outputManager, EnphaseCollectorProperties properties) {
		this.outputManager = outputManager;
		this.properties = properties;
	}

	private SystemInfo getSystemInfo() {
		List<Metric> metrics = outputManager.getMetrics();

		Optional<Metric> metric;

		metric = getMetric(metrics, "solar.production.current");

		int productionWatts = metric.isPresent() ? Math.round(metric.get().getValue()) : 0;

		metric = getMetric(metrics, "solar.consumption.current");

		int consumptionWatts = metric.isPresent() ? Math.round(metric.get().getValue()) : 0;

		return new SystemInfo(productionWatts, consumptionWatts, outputManager.getInvertorCount(), true, outputManager.getCollectionTime());
	}

	// Generate main page from template
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("enphase_info", getSystemInfo());
		model.addAttribute("software_version", outputManager.getSoftwareVersion());
		model.addAttribute("serial_number", outputManager.getSerialNumber());
		model.addAttribute("refresh_interval", properties.getRefreshSeconds());
		return "index";
	}

	@GetMapping(value = "/production", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Integer production() {
		List<Metric> metrics = outputManager.getMetrics();
		Optional<Metric> metric = getMetric(metrics, "solar.production.current");
		return Integer.valueOf(metric.isPresent() ? Math.round(metric.get().getValue()) : 0);
	}

	@GetMapping(value = "/consumption", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public Integer consumption() {
		List<Metric> metrics = outputManager.getMetrics();
		Optional<Metric> metric = getMetric(metrics, "solar.consumption.current");
		return Integer.valueOf(metric.isPresent() ? Math.round(metric.get().getValue()) : 0);
	}

	private Optional<Metric> getMetric(List<Metric> metrics, String name) {
		return metrics.stream().filter(metric -> metric.getName().equalsIgnoreCase(name)).findFirst();
	}
}
