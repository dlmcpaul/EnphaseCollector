package com.hz.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class StartupService {

	private final SummaryService summaryService;
	private final ElectricityRateService electricityRateService;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void applicationReady() {
		electricityRateService.upgradeRates();
		summaryService.upgradeConversion();
		summaryService.createSummaries();
	}


}
