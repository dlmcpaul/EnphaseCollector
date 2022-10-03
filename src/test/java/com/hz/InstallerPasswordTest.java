package com.hz;

import com.hz.configuration.SecretConfig;
import com.hz.configuration.SkipWhenMissingFile;
import com.hz.utils.InstallerPasswordCalculator;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = SecretConfig.class)
@TestPropertySource(locations = "classpath:secrets.properties")
@SkipWhenMissingFile(filename="secrets.properties")
class InstallerPasswordTest {

	@Autowired
	private SecretConfig secrets;

	@Test
	void testPasswordDecoder() {
		assertEquals(secrets.getInstallerPassword(), InstallerPasswordCalculator.getPassword(secrets.getEnvoySerialNumber()));
		assertEquals("7edE24ed", InstallerPasswordCalculator.getPassword("123456789012"));
		assertEquals("522cd7De", InstallerPasswordCalculator.getPassword("999999999999"));
		assertEquals("Bc45A7ef", InstallerPasswordCalculator.getPassword("111111111111"));
		assertEquals("285A97bf", InstallerPasswordCalculator.getPassword("000000000000"));
	}
}
