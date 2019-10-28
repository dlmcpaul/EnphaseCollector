package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PowerMeter {
	private String eid;
	private long timestamp;
	private BigDecimal actEnergyDlvd;
	private BigDecimal actEnergyRcvd;
	private BigDecimal apparentEnergy;
	private BigDecimal reactEnergyLagg;
	private BigDecimal reactEnergyLead;
	private BigDecimal instantaneousDemand;
	private BigDecimal activePower;
	private BigDecimal apparentPower;
	private BigDecimal reactivePower;
	private BigDecimal pwrFactor;
	private BigDecimal voltage;
	private BigDecimal current;
	private BigDecimal freq;

	@JsonProperty(value="channels")
	private List<Channel> channelList;
}
