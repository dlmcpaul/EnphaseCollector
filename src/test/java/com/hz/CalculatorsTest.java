package com.hz;

import com.hz.utils.Calculators;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorsTest {

	private static final String FOUR_WEEKS = "4Weeks";
	private static final String SEVEN_DAYS = "7Days";
	private static final String THREE_MONTHS = "3Months";

	@Test
	void WeekTests() {
		LocalDate startOfWeek = Calculators.calculateStartDateFromDuration(LocalDate.of(2022, 5, 29), ChronoUnit.WEEKS, FOUR_WEEKS);
		LocalDate endOfWeek = Calculators.calculateEndDateFromDuration(LocalDate.of(2022, 5, 29), ChronoUnit.WEEKS);

		assertEquals(DayOfWeek.SUNDAY, startOfWeek.getDayOfWeek());
		assertEquals(DayOfWeek.SATURDAY, endOfWeek.getDayOfWeek());

		assertEquals(LocalDate.of(2022, 5, 1), startOfWeek);
		assertEquals(LocalDate.of(2022, 5, 28), endOfWeek);
	}

	@Test
	void MonthTests() {
		LocalDate quarterStart = Calculators.calculateStartDateFromDuration(LocalDate.of(2022, 5, 29), ChronoUnit.MONTHS, THREE_MONTHS);
		LocalDate quarterEnd = Calculators.calculateEndDateFromDuration(LocalDate.of(2022, 5, 29), ChronoUnit.MONTHS);

		assertEquals(LocalDate.of(2022, 2, 1), quarterStart);
		assertEquals(LocalDate.of(2022, 4, 30), quarterEnd);
	}

	@Test
	void DayTests() {
		LocalDate sevenDaysAgo = Calculators.calculateStartDateFromDuration(SEVEN_DAYS);
		LocalDate yesterday = Calculators.calculateEndDateFromDuration(SEVEN_DAYS);

		assertEquals(LocalDate.now().minusDays(7), sevenDaysAgo);
		assertEquals(LocalDate.now().minusDays(1L), yesterday);
	}
}
