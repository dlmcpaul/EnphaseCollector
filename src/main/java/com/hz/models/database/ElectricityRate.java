package com.hz.models.database;

import com.hz.models.interfaces.RateInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityRate implements RateInterface {
	@Id
	private LocalDate effectiveDate;
	private Double paymentPerKiloWatt = 0.0;
	private Double chargePerKiloWatt = 0.0;
	private Double dailySupplyCharge = 0.0;

	public ElectricityRate(LocalDate effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public ElectricityRate(Double paymentPerKiloWatt, Double chargePerKiloWatt, Double dailySupplyCharge) {
		this.paymentPerKiloWatt = paymentPerKiloWatt;
		this.chargePerKiloWatt = chargePerKiloWatt;
		this.dailySupplyCharge = dailySupplyCharge;
	}
}
