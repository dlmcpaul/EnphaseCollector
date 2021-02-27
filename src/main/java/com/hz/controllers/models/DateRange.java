package com.hz.controllers.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

@NoArgsConstructor
@Data
public class DateRange {
	// Because this object is passed to and from the browser
	// it needs to conform to RFC3339 Section 5.6
	// which sets the standard for the date wire format
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate fromDate = LocalDate.now().minusMonths(3);    // Default to 3 months ago
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate toDate = LocalDate.now().minusDays(1);        // Default to Yesterday

	public long daysInPeriod() {
		return DAYS.between(fromDate, toDate) + 1;
	}

	public String toString() {
		return fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " -> " + toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}
