package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CurrentNetwork {
	private String ssid;
	private String status;
	@JsonProperty(value="ip_address")
	private String ipAddress;
	@JsonProperty(value="gateway_ip")
	private String gatewayIp;
	@JsonProperty(value="security_mode")
	private String securityMode;
	private String encryptionType;
	@JsonProperty(value="ap_bssid")
	private String apBssid;
	private String channel;
	private int bars;
}
