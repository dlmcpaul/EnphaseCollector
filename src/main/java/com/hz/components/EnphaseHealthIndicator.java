package com.hz.components;

import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnphaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class EnphaseHealthIndicator implements HealthIndicator {

	@Autowired
	private EnphaseService enphaseService;

	@Autowired
	private EnvoyInfo envoyInfo;

	public Health health() {
		if (enphaseService.isOk()) {
			return Health.up().withDetail("version", envoyInfo.getSoftwareVersion()).build();
		}
		return Health.down().build();
	}

}
