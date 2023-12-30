package com.hz.controllers.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.TreeSet;

@Data
@NoArgsConstructor
public class Timeline {
	Comparator<TimelineEntry> byDate = Comparator.comparing(TimelineEntry::getDate);

	private LocalDate earliestEntry;
	private TreeSet<TimelineEntry> timelineEntryList = new TreeSet<>(byDate);

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
