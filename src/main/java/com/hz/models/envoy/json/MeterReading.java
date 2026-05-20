package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeterReading {
	private BigDecimal currW;
	private BigDecimal actPower;
	private BigDecimal apprntPwr;
	private BigDecimal reactPwr;
	private BigDecimal whDlvdCum;
	private BigDecimal whRcvdCum;
	private BigDecimal varhLagCum;
	private BigDecimal varhLeadCum;
	private BigDecimal vahCum;
	private BigDecimal rmsVoltage;
	private BigDecimal rmsCurrent;
	private BigDecimal pwrFactor;
	private BigDecimal freqHz;
}
