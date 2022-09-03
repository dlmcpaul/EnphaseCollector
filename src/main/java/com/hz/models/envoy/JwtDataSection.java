package com.hz.models.envoy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hz.utils.Convertors;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JwtDataSection {
	@JsonProperty(value="aud")
	private String serialNumber;
	@JsonProperty(value="iss")
	private String issuer;
	private String enphaseUser;
	@JsonProperty(value="exp")
	private long expires;
	@JsonProperty(value="iat")
	private long issuerDate;
	private String jti;
	@JsonProperty(value="username")
	private String userName;

	public LocalDateTime getExpires() {
		return Convertors.convertToLocalDateTime(this.expires);
	}

	public LocalDateTime getIssuerDate() {
		return Convertors.convertToLocalDateTime(this.issuerDate);
	}

}
