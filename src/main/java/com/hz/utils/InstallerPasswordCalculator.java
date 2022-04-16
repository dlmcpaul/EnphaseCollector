package com.hz.utils;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.stream.Collectors;

@Log4j2
public class InstallerPasswordCalculator {
	public static final String REALM = "enphaseenergy.com";
	public static final String USERNAME = "installer";

	private static int countZero;
	private static int countOne;

	private InstallerPasswordCalculator() {}

	public static String getPassword(String serialNumber) {
		String digest = getDigest(serialNumber);

		countZero = Integer.min(count(digest, '0'), 20);
		countOne = Integer.min(count(digest, '1'), 26);

		return calculatePassword(getLast8Chars(digest));
	}

	@SneakyThrows
	private static String getDigest(String serialNumber) {
		String hashCode = "[e]" + USERNAME + "@" + REALM + "#" + serialNumber + " EnPhAsE eNeRgY ";

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(hashCode.getBytes());
		return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
	}

	private static int count(String input, char value) {
		return (int) input.chars().filter(ch -> ch == value).count();
	}

	private static String getLast8Chars(String input) {
		return new StringBuffer(input.substring(input.length()-8)).reverse().toString();
	}

	private static Character decode(int value) {
		int result;

		switch (countZero) {
			case -1     : countZero++;
			case 3,6,9  : countZero--;
		}

		switch (countOne) {
			case -1     : countOne++;
			case 9,15   : countOne--;
		}

		result = switch (value) {
			case '0' -> ("f".codePointAt(0) + countZero--);
			case '1' -> ("@".codePointAt(0) + countOne--);
			default -> value;
		};

		return (char) result;

	}

	private static String calculatePassword(String encodedPassword) {
		return encodedPassword.chars()
				.map(InstallerPasswordCalculator::decode)
				.mapToObj(Character::toString)
				.collect(Collectors.joining());
	}

}
