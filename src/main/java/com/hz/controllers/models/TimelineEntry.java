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
		HIGHEST_OUTPUT,
		HIGHEST_GRID_EXPORT
	}

	private LocalDate date;
	private EntryType entryType;
	private BigDecimal value;

	public String getDescription() {
		return switch (entryType) {
			case HIGHEST_PRODUCTION -> "Highest Solar Produced";
			case HIGHEST_GRID_IMPORT -> "Highest Imported from Grid";
			case HIGHEST_OUTPUT -> "Peak Power Achieved";
			case HIGHEST_GRID_EXPORT -> "Highest Excess Exported";
			case null, default -> "Unknown";
		};
	}

	public String getValueAsString() {
		return switch (entryType) {
			case HIGHEST_OUTPUT -> value.toPlainString() + " W";
			case null, default -> value.toPlainString() + " kWh";
		};
	}

	public String getIcon() {
		return switch (entryType) {
			case HIGHEST_PRODUCTION -> "fas fa-sun";
			case HIGHEST_GRID_IMPORT -> "fas fa-thumbs-down";
			case HIGHEST_OUTPUT -> "fas fa-arrow-circle-up";
			case HIGHEST_GRID_EXPORT -> "fas fa-arrow-up";
			case null, default -> "";
		};
	}

	public int getYear() {
		return date.getYear();
	}
}
