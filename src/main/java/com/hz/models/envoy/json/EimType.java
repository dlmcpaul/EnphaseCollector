package com.hz.models.envoy.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Created by David on 23-Oct-17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EimType extends TypeBase {
	private String measurementType;
	private BigDecimal varhLeadLifetime;
	private BigDecimal varhLagLifetime;
	private BigDecimal vahLifetime;
	private BigDecimal rmsCurrent;
	private BigDecimal rmsVoltage;
	private BigDecimal reactPwr;
	private BigDecimal apprntPwr;
	private int pwrFactor;
	private BigDecimal whToday;
	private BigDecimal whLastSevenDays;
	private BigDecimal vahToday;
	private BigDecimal varhLeadToday;
	private BigDecimal varhLagToday;
}
