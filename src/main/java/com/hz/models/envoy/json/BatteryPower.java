package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatteryPower {
	@JsonProperty(value="devices:")
	private List<BatteryDevice> devices;

	public BatteryPower() {
		devices = new ArrayList<>();
	}
}
