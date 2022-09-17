package com.hz.models.envoy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hz.models.envoy.xml.EnvoyInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Base64;

@Data
public class AuthorisationToken {

	protected enum AuthorisationTokenType {
		V5,             // Original User Password
		V7_PROVIDED,    // Jwt is Provided as part of configuration
		V7_FETCHABLE    // Jwt needs to be fetched from enphase website
	}

	private String user;
	private String password;
	private String serialNo;
	private String jwt;
	private LocalDateTime expires;
	private AuthorisationTokenType tokenType;

	public AuthorisationToken(String user, String password) {
		this.tokenType = AuthorisationTokenType.V5;
		this.user = user;
		this.password = password;
	}

	public AuthorisationToken(String jwt, LocalDateTime expires) {
		this.tokenType = AuthorisationTokenType.V7_PROVIDED;
		this.jwt = jwt;
		this.expires = expires;
	}

	public AuthorisationToken(String user, String password, String serialNo, LocalDateTime expires) {
		this.tokenType = AuthorisationTokenType.V7_FETCHABLE;
		this.user = user;
		this.password = password;
		this.serialNo = serialNo;
		this.expires = expires;
	}

	public boolean isV5() {
		return tokenType.equals(AuthorisationTokenType.V5);
	}

	public boolean hasExpired() {
		return (tokenType.equals(AuthorisationTokenType.V5)) ? false : expires.isBefore(LocalDateTime.now());
	}

	public boolean canFetchToken() {
		return this.tokenType == AuthorisationTokenType.V7_FETCHABLE;
	}

	public void updateToken(String jwt) throws JsonProcessingException {
		this.jwt = jwt;
		this.expires = getJwtDataSection(jwt).getExpires();
	}

	private static JwtDataSection getJwtDataSection(String jwt) throws JsonProcessingException {
		// JWT cannot be validated because the key is hidden from us. *Dangerous*
		String[] split = jwt.split("\\.");

		String data = new String(Base64.getDecoder().decode(split[1]));

		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return jsonMapper.readValue(data, JwtDataSection.class);
	}

	public static AuthorisationToken makeV5(EnvoyInfo envoyInfo, String suppliedPassword) {
		return new AuthorisationToken("envoy", (suppliedPassword == null || suppliedPassword.isEmpty()) ? envoyInfo.getDefaultPassword() : suppliedPassword);
	}

	public static AuthorisationToken makeV7TokenProvided(String jwt) throws JsonProcessingException {
		return new AuthorisationToken(jwt, getJwtDataSection(jwt).getExpires());
	}

	public static AuthorisationToken makeV7TokenFetched(String enphaseUser, String enphasePassword, String serialNo) {
		return new AuthorisationToken(enphaseUser, enphasePassword, serialNo, LocalDateTime.now());
	}
}
