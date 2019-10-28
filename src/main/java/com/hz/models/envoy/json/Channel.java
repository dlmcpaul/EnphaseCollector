package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel {
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
}
