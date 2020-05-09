package com.hz.components;

import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.services.EnphaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnphaseHealthIndicator implements HealthIndicator {

	private final EnphaseService enphaseService;
	private final EnvoyInfo envoyInfo;

	public Health health() {
		if (enphaseService.isOk()) {
			return Health.up().withDetail("version", envoyInfo.getSoftwareVersion()).build();
		}
		return Health.down().build();
	}

}
