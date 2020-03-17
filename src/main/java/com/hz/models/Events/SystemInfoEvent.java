package com.hz.models.Events;

import com.hz.models.database.EnvoySystem;
import org.springframework.context.ApplicationEvent;

public class SystemInfoEvent extends ApplicationEvent {
	private EnvoySystem envoySystem;

	public SystemInfoEvent(Object source, EnvoySystem envoySystem) {
		super(source);
		this.envoySystem = envoySystem;
	}

	public EnvoySystem getEnvoySystem() {
		return envoySystem;
	}
}
