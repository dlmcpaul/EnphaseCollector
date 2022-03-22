package com.hz;

import com.hz.controllers.models.DateRange;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class DateRangeTest {

	private static Validator validator;

	@BeforeAll
	public static void setupValidatorInstance() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void DefaultDateRangeTest() {
		DateRange dateRange = new DateRange();
		Set<ConstraintViolation<DateRange>> violations;

		log.info("Test default DateRange constructor");
		violations = validator.validate(dateRange);
		assertEquals(violations.size(),0);

		log.info("Test DateRange constructor getDuration calculation");
		dateRange.setFrom(LocalDate.parse("2021-12-22", DateTimeFormatter.ISO_DATE));
		dateRange.setTo(LocalDate.parse("2022-03-21", DateTimeFormatter.ISO_DATE));
		assertEquals(dateRange.getDuration(),90);

		log.info("Test DateRange NotNull Validation");
		dateRange.setFrom(null);
		violations = validator.validate(dateRange);
		assertEquals(violations.size(),1);

		log.info("Test DateRange ToLessThanFrom Validation");
		dateRange.setFrom(LocalDate.parse("2022-03-21", DateTimeFormatter.ISO_DATE));
		dateRange.setTo(LocalDate.parse("2021-12-22", DateTimeFormatter.ISO_DATE));
		violations = validator.validate(dateRange);
		assertEquals(violations.size(),1);
	}
}
