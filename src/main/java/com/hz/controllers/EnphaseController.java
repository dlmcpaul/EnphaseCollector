package com.hz.controllers;

import com.hz.services.OutputManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by David on 23-Oct-17.
 */
@Controller
public class EnphaseController {

	private final OutputManager outputManager;

	@Autowired
	public EnphaseController(OutputManager outputManager) {
		this.outputManager = outputManager;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("metrics", outputManager.collect());
		return "index";
	}
}
