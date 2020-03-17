package com.hz.models.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class EnvoySystem {
	@Id
	private Long id = 1L;

	private boolean wifi;
	private String network;
	private LocalDateTime lastCommunication;
	private LocalDateTime lastReadTime;
	private String envoyVersion;
	private String envoySerial;
	private int panelCount;

	public EnvoySystem(String envoySerial, String envoyVersion, LocalDateTime lastCommunication, LocalDateTime lastReadTime, int panelCount, boolean wifi, String network) {
		this.envoySerial = envoySerial;
		this.envoyVersion = envoyVersion;
		this.lastCommunication = lastCommunication;
		this.lastReadTime = lastReadTime;
		this.panelCount = panelCount;
		this.wifi = wifi;
		this.network = network;
	}

	public LocalDateTime getLastCommunication() {
		if (lastCommunication == null) {
			lastCommunication = LocalDateTime.now();
		}

		return lastCommunication;
	}

	public LocalDateTime getLastReadTime() {
		if (lastReadTime == null) {
			lastReadTime = LocalDateTime.now();
		}

		return lastReadTime;
	}

}
