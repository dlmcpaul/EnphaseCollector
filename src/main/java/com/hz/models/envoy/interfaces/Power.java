package com.hz.models.envoy.interfaces;

import java.math.BigDecimal;

public interface Power {
	BigDecimal getActivePower();
	BigDecimal getEnergyDelivered();
	BigDecimal getEnergyReceived();
	BigDecimal getCurrentPower();
	String getEid();

	long getTimestamp();
	BigDecimal getVoltage();
}
