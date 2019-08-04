package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Wireless {
	private boolean supported;
	private boolean present;
	private boolean configured;
	private boolean up;
	private boolean carrier;
	@JsonProperty(value="current_network")
	private CurrentNetwork currentNetwork;
	@JsonProperty(value="device_info")
	private DeviceInfo deviceInfo;
	@JsonProperty(value="ap_mode")
	private APMode apMode;
	@JsonProperty(value="selected_region")
	private String selectedRegion;
	private List<String> regions;
}
