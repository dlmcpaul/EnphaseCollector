package com.hz.controllers.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.TreeMap;

import static com.hz.controllers.models.TimelineEntry.EntryType.EMPTY_TIMELINE;

@Data
public class Timeline {
	private LocalDate earliestEntry;
	private TreeMap<String, TimelineEntry> timelineEntryList = new TreeMap<>();

	public Timeline() {
		this.earliestEntry = LocalDate.now();
		this.addTimeLine(earliestEntry, EMPTY_TIMELINE, BigDecimal.ZERO);
	}

	public Timeline(LocalDate earliestEntry) {
		this.earliestEntry = earliestEntry;
	}

	public void addTimeLineEntry(TimelineEntry e) {
		if (e.getDate() != null) {
			this.timelineEntryList.put(String.valueOf(e.getDate()) + e.getEntryType(), e);
		}
	}

	public void addTimeLine(LocalDate date, TimelineEntry.EntryType entryType, BigDecimal value) {
		this.addTimeLineEntry(new TimelineEntry(date, entryType, value));
	}

	public Collection<TimelineEntry> values() {
		return this.timelineEntryList.values();
	}
}
