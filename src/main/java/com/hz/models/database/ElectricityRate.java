package com.hz.models.database;

import com.hz.models.interfaces.RateInterface;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.Hibernate;

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
		if (o instanceof ElectricityRate that) {
			if (this == o) return true;
			if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
			return Objects.equals(this.effectiveDate, that.effectiveDate);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
