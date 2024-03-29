package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
@Log4j2
class BandTests {
	@Autowired
	private EnphaseCollectorProperties properties;

	@Test
	void tests() {
		assertNotNull(properties.getBands());
		assertEquals(2, properties.getBands().size());
		assertTrue(properties.getBands().get(0).getFrom().equalsIgnoreCase("0730"));
		assertTrue(properties.getBands().get(0).getTo().equalsIgnoreCase("1100"));
		assertTrue(properties.getBands().get(0).getColour().equalsIgnoreCase("rgb(200, 60, 60, .2)"));
		assertTrue(properties.getBands().get(1).getFrom().equalsIgnoreCase("1700"));
		assertTrue(properties.getBands().get(1).getTo().equalsIgnoreCase("2100"));
		assertTrue(properties.getBands().get(1).getColour().equalsIgnoreCase("rgb(200, 60, 60, .2)"));

		LocalDate localDate = LocalDate.now();
		LocalDateTime localFrom = LocalTime.parse("1500", DateTimeFormatter.ofPattern("HHmm")).atDate(localDate);
		log.info("from {} {}", localFrom, ZonedDateTime.of(localFrom, ZoneId.systemDefault()).toInstant().toEpochMilli());

		LocalDateTime localTo = LocalTime.parse("1530", DateTimeFormatter.ofPattern("HHmm")).atDate(localDate);
		log.info("to {} {}", localTo, ZonedDateTime.of(localTo, ZoneId.systemDefault()).toInstant().toEpochMilli());
	}
}
