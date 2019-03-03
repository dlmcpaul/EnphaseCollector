package com.hz.models.database;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
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
