package com.hz.models.envoy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRequest {
	@JsonProperty(value="session_id")
	public String sessionId;
	@JsonProperty(value="serial_num")
	public String serialNo;
	@JsonProperty(value="username")
	public String userName;
}
