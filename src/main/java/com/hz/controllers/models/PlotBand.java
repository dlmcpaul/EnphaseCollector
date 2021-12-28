package com.hz.controllers.models;

import lombok.Data;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Data
public class PlotBand {
	private long from;
	private long to;
	private String color;

	public PlotBand(String from, String to, String colour) {
		this.color = colour;

		LocalDate today = LocalDate.now();
		LocalDateTime localFrom = LocalTime.parse(from, DateTimeFormatter.ofPattern("HHmm")).atDate(today);
		this.from = ZonedDateTime.of(localFrom, ZoneId.systemDefault()).toInstant().toEpochMilli();

		LocalDateTime localTo = LocalTime.parse(to, DateTimeFormatter.ofPattern("HHmm")).atDate(today);
		this.to = ZonedDateTime.of(localTo, ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
}
