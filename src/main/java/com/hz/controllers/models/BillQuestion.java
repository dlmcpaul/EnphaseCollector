package com.hz.controllers.models;

import com.hz.models.interfaces.RateInterface;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@NoArgsConstructor
@Log4j2
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
