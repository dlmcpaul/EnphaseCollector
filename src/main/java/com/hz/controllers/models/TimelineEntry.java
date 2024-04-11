package com.hz.controllers.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntry {
	public enum EntryType {
		HIGHEST_PRODUCTION,
		HIGHEST_GRID_IMPORT,
		HIGHEST_POWER_ACHIEVED,
		HIGHEST_SOLAR_EXPORT,
		EMPTY_TIMELINE
	}

	private LocalDate date;
	private EntryType entryType;
	private BigDecimal value;

	private String getDescription() {
		return switch (entryType) {
			case HIGHEST_PRODUCTION -> "Highest Solar Produced";
			case HIGHEST_GRID_IMPORT -> "Highest Grid Imported";
			case HIGHEST_POWER_ACHIEVED -> "Peak Power Achieved";
			case HIGHEST_SOLAR_EXPORT -> "Highest Solar Exported";
			case EMPTY_TIMELINE -> "Nothing Here Yet";
			case null -> "Unknown";
		};
	}

	private String getValueAsString() {
		return switch (entryType) {
			case HIGHEST_POWER_ACHIEVED -> value.toPlainString() + " W";
			case EMPTY_TIMELINE -> "";
			case null, default -> value.toPlainString() + " kWh";
		};
	}

	public String getIcon() {
		return switch (entryType) {
			case HIGHEST_PRODUCTION -> "fas fa-sun";
			case HIGHEST_GRID_IMPORT -> "fas fa-thumbs-down";
			case HIGHEST_POWER_ACHIEVED -> "fas fa-arrow-up";
			case HIGHEST_SOLAR_EXPORT -> "fas fa-thumbs-up";
			case EMPTY_TIMELINE -> "fas fa-arrow-down";
			case null -> "";
		};
	}

	public int getYear() {
		return date.getYear();
	}

	public String getSubtitle() {
		return this.getDescription() + " " + this.getValueAsString();
	}
}
