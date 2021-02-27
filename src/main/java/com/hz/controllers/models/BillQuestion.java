package com.hz.controllers.models;

import com.hz.models.interfaces.RateInterface;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BillQuestion implements RateInterface {
	private DateRange dateRange = new DateRange();

	private Double paymentPerKiloWatt = 0.0;
	private Double chargePerKiloWatt = 0.0;
	private Double dailySupplyCharge = 0.0;

	public String toString() {
		return dateRange.toString();
	}
}
