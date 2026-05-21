package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hz.models.envoy.interfaces.Power;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PowerMeter implements Power {
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

	public PowerMeter() {
	}

	public PowerMeter(BigDecimal activePower, BigDecimal voltage) {
		this.activePower = activePower;
		this.voltage = voltage;
	}

	public PowerMeter(BigDecimal activePower, BigDecimal voltage, long timestamp) {
		this.activePower = activePower;
		this.voltage = voltage;
		this.timestamp = timestamp;
	}

	@JsonProperty(value="channels")
	private List<Channel> channelList;

	@Override
	public BigDecimal getActivePower() {
		return activePower;
	}

	@Override
	public BigDecimal getEnergyDelivered() {
		return actEnergyDlvd;
	}

	@Override
	public BigDecimal getEnergyReceived() {
		return actEnergyRcvd;
	}

	@Override
	public BigDecimal getCurrentPower() {
		return BigDecimal.ZERO;
	}

	@Override
	public String getEid() {
		return eid;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public BigDecimal getVoltage() {
		return voltage;
	}
}
