package com.hz.components;

import com.hz.services.EnphaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class EnphaseHealthIndicator implements HealthIndicator {

	@Autowired
	private EnphaseService enphaseService;

	public Health health() {
		if (enphaseService.isOk()) {
			return Health.up().withDetail("version",enphaseService.getVersion()).build();
		}
		return Health.down().build();
	}

}
