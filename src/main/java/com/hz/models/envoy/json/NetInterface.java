package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NetInterface {
	private String type;
	@JsonProperty(value="interface")
	private  String interfaceName;
	private  String mac;
	private  boolean dhcp;
	private String ip;
	@JsonProperty(value="signal_strength")
	private int signalStrength;
	@JsonProperty(value="signal_strength_max")
	private int getSignalStrengthMax;
	private boolean carrier;
	private boolean supported;
	private boolean present;
	private boolean configured;
	private String status;
}
