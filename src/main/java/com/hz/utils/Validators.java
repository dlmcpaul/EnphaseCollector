package com.hz.utils;

public class Validators {
	private Validators() {}

	public static boolean isValidDuration(String duration) {
		return duration.toUpperCase().matches("[0-9](DAYS|WEEKS|MONTHS)");
	}
}
