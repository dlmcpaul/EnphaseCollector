package com.hz;

import com.hz.utils.InstallerPasswordCalculator;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class InstallerPasswordTest {

	@Test
	void testPasswordDecoder() {
		assertEquals("7edE24ed", InstallerPasswordCalculator.getPassword("123456789012"));
		assertEquals("522cd7De", InstallerPasswordCalculator.getPassword("999999999999"));
		assertEquals("Bc45A7ef", InstallerPasswordCalculator.getPassword("111111111111"));
		assertEquals("285A97bf", InstallerPasswordCalculator.getPassword("000000000000"));
	}
}
