package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hz.models.envoy.interfaces.Power;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeterReport implements Power {
	private long createdAt;
	private String reportType;
	private MeterReading cumulative;
	private List<MeterReading> lines = new ArrayList<>();

	@Override
	public BigDecimal getActivePower() {
		return cumulative.getActPower();
	}

	@Override
	public BigDecimal getEnergyDelivered() {
		return cumulative.getWhDlvdCum();
	}

	@Override
	public BigDecimal getEnergyReceived() {
		return cumulative.getWhRcvdCum();
	}

	@Override
	public BigDecimal getCurrentPower() {
		return cumulative.getCurrW();
	}

	@Override
	public String getEid() {
		return null;
	}

	@Override
	public long getTimestamp() {
		return createdAt;
	}

	@Override
	public BigDecimal getVoltage() {
		return cumulative.getRmsVoltage();
	}

	public int getPhaseCount() {
		return lines.isEmpty() ? 1 : lines.size();
	}
}
