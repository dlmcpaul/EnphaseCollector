package com.hz.models.envoy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogInResponse {
	String message;
	@JsonProperty(value="session_id")
	String sessionId;
	@JsonProperty(value="manager_token")
	String managerToken;
	@JsonProperty(value="is_consumer")
	boolean isConsumer;

	public boolean isSuccess() {
		return message.equalsIgnoreCase("success");
	}
}
