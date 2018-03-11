package com.hz.controllers;

import com.hz.models.System;
import com.hz.services.EnphaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by David on 23-Oct-17.
 */
@RestController
public class EnphaseController {

	private final EnphaseService enphaseService;

	@Autowired
	public EnphaseController(EnphaseService enphaseService) {
		this.enphaseService = enphaseService;
	}

	@RequestMapping("/collect")
	public System getStatus() {

		try {
			System system = enphaseService.collect();
			enphaseService.uploadMetrics(system);
			return system;

		} catch (IOException e) {

		}
		return null;
	}
}
