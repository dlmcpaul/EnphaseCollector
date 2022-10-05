package com.hz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hz.configuration.SecretConfig;
import com.hz.configuration.SkipWhenMissingFile;
import com.hz.models.envoy.JwtDataSection;
import com.hz.utils.EnphaseJWTExtractor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = SecretConfig.class)
@TestPropertySource(locations = "classpath:secrets.properties")
@SkipWhenMissingFile(filename="secrets.properties")
public class JwtFetchTest {

	@Autowired
	private SecretConfig secrets;

	@Test
	void testFetchingJWTFromWebSite() {
		try {
			log.info(secrets.getEnphaseWebUser());
			String jwt = EnphaseJWTExtractor.fetchJWT(secrets.getEnphaseWebUser(), secrets.getEnphaseWebPassword(), secrets.getEnvoySerialNumber());
			assertFalse(jwt.isEmpty());

			// JWT cannot be validated because the key is hidden from us.
			String[] split = jwt.split("\\.");
			assertEquals(3, split.length);

			String data = new String(Base64.getDecoder().decode(split[1]));

			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			JwtDataSection jwtDataSection = jsonMapper.readValue(data, JwtDataSection.class);

			log.info("jti {}", jwtDataSection.getJti());
			log.info("Issued {}", jwtDataSection.getIssuerDate());
			log.info("expires {}", jwtDataSection.getExpires());

			assertTrue(jwtDataSection.getSerialNumber().equalsIgnoreCase(secrets.getEnvoySerialNumber()));
			assertTrue(jwtDataSection.getIssuerDate().isBefore(LocalDateTime.now().plus(1, MINUTES)));
			assertTrue(jwtDataSection.getExpires().isEqual(jwtDataSection.getIssuerDate().plus(12, MONTHS)));
		} catch (IOException e) {
			log.error("ERROR: {}", e.getMessage(), e);
			fail();
		}
	}


}
