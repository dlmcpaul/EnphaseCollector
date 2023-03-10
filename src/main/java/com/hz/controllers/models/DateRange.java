package com.hz.controllers.models;

import com.hz.annotations.ToLessThanFrom;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

// Because this object is passed to and from the browser
// it needs to conform to RFC3339 Section 5.6
// which sets the standard for the date wire format

@Data
@ToLessThanFrom
public class DateRange {
	@Past(message = "Date must be in the past")
	@NotNull
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	LocalDate from;
	@PastOrPresent(message="Date must be in the past or in the present")
	@NotNull
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	LocalDate to;

	public DateRange() {
		this.from = LocalDate.now().minusMonths(3);
		this.to = LocalDate.now().minusDays(1);
	}

	public long getDuration() {
		return DAYS.between(from, to) + 1;
	}
}
