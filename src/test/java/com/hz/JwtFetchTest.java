package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.configuration.SecretConfig;
import com.hz.configuration.SkipWhenMissingFile;
import com.hz.utils.EnphaseJWTExtractor;
import com.hz.utils.EnphaseJWTVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MissingClaimException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = {SecretConfig.class, EnphaseCollectorProperties.class})
@TestPropertySource(locations = {"classpath:secrets.properties", "classpath:application.properties"})
@SkipWhenMissingFile(filename="secrets.properties")
class JwtFetchTest {

	@Autowired
	EnphaseCollectorProperties properties;

	@Autowired
	SecretConfig secrets;

	private LocalDateTime convertToLocal(Date in) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(in.getTime()), ZoneId.systemDefault());
	}

	@Test
	void testFetchingJWTFromWebSite() {
		try {
			log.info("Retrieving a JWT for user {}", secrets.getEnphaseWebUser());

			String jws = EnphaseJWTExtractor.fetchJWT(secrets.getEnphaseWebUser(), secrets.getEnphaseWebPassword(), secrets.getEnvoySerialNumber());
			assertFalse(jws.isEmpty());

			Claims jwt = EnphaseJWTVerifier.verifyClaims(secrets.getEnphaseWebUser(), properties.getPublicKey(), secrets.getEnvoySerialNumber(), jws);

			log.info("jws found and verified");
			log.info("jti {}", jwt.get("jti", String.class));
			log.info("Issued {}", jwt.getIssuedAt());
			log.info("expires {}", jwt.getExpiration());

			assertTrue(jwt.getAudience().stream().findAny().orElseThrow().equalsIgnoreCase(secrets.getEnvoySerialNumber()));
			assertTrue(convertToLocal(jwt.getIssuedAt()).isBefore(LocalDateTime.now().plusMinutes(1)));
			assertTrue(convertToLocal(jwt.getExpiration()).isEqual(convertToLocal(jwt.getIssuedAt()).plusDays(365)));
		} catch (MissingClaimException mce) {
			log.error("Required Claim not found: {}", mce.getMessage(), mce);
			fail();
		} catch (IncorrectClaimException ice) {
			log.error("Claim was present but value did not match: {}", ice.getMessage(), ice);
			fail();
		} catch (IOException | GeneralSecurityException e) {
			log.error("ERROR: {}", e.getMessage(), e);
			fail();
		}
	}

}
