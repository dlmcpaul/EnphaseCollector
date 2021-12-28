package com.hz.models.database;

import com.hz.models.interfaces.RateInterface;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		ElectricityRate that = (ElectricityRate) o;
		return Objects.equals(effectiveDate, that.effectiveDate);
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
