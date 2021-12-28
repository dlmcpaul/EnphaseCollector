package com.hz.controllers.models;

import com.hz.models.interfaces.RateInterface;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Data
public class BillQuestion implements RateInterface {
	@Valid
	private DateRange dateRange = new DateRange();

	@NotNull
	private Double paymentPerKiloWatt = 0.0;
	@NotNull
	private Double chargePerKiloWatt = 0.0;
	@NotNull
	private Double dailySupplyCharge = 0.0;

	public String toString() {
		return dateRange.toString();
	}
}
