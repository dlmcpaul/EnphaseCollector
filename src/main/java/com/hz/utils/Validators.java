package com.hz.utils;

public class Validators {
	private Validators() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isValidDuration(String duration) {
		return duration.toUpperCase().matches("\\d(DAYS|WEEKS|MONTHS|QUARTERS)");
	}
}
