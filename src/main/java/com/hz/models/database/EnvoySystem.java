package com.hz.models.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EnvoySystem implements Serializable {
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

	@Override
	public boolean equals(Object o) {
		if (o instanceof EnvoySystem that) {
			if (this == o) return true;
			if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
			return Objects.equals(this.id, that.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
