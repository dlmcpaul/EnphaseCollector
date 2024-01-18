package com.hz.controllers.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.TreeSet;

import static com.hz.controllers.models.TimelineEntry.EntryType.EMPTY_TIMELINE;

@Data
public class Timeline {
	private final Comparator<TimelineEntry> byDate = Comparator.comparing(TimelineEntry::getDate);

	private LocalDate earliestEntry;
	private TreeSet<TimelineEntry> timelineEntryList = new TreeSet<>(byDate);

	public Timeline() {
		this.earliestEntry = LocalDate.now();
		this.addTimeLine(earliestEntry, EMPTY_TIMELINE, BigDecimal.ZERO);
	}

	public Timeline(LocalDate earliestEntry) {
		this.earliestEntry = earliestEntry;
	}

	public void addTimeLineEntry(TimelineEntry e) {
		this.timelineEntryList.add(e);
	}

	public void addTimeLine(LocalDate date, TimelineEntry.EntryType entryType, BigDecimal value) {
		this.addTimeLineEntry(new TimelineEntry(date, entryType, value));
	}
}
