package com.hz.models.envoy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebToken {
	@JsonProperty(value="generation_time")
	private String generation;
	private String token;
	@JsonProperty(value="expires_at")
	private String expiry;
}
