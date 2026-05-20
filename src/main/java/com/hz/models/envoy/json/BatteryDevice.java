package com.hz.models.envoy.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatteryDevice {
	@JsonProperty(value="serial_num")
	private long serialNumber;
	@JsonProperty(value="real_power_mw")
	private int realPowerMW;
	@JsonProperty(value="apparent_power_mva")
	private int apparentPowerMVA;
	@JsonProperty(value="soc")
	private int stateOfCharge;
}
