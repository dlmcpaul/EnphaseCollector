package com.hz.controllers;

import com.hz.services.EnphaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by David on 23-Oct-17.
 */
@Controller
public class EnphaseController {

	private final EnphaseService enphaseService;

	@Autowired
	public EnphaseController(EnphaseService enphaseService) {
		this.enphaseService = enphaseService;
	}

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("metrics", enphaseService.collect());
		return "index";
	}
}
