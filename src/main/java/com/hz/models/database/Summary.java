package com.hz.models.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Summary {

	@Id
	private LocalDate date;
	private BigDecimal gridImport = BigDecimal.ZERO;
	private BigDecimal gridExport = BigDecimal.ZERO;
	private BigDecimal consumption = BigDecimal.ZERO;
	private BigDecimal production = BigDecimal.ZERO;
	private Long highestOutput = 0L;
	private BigDecimal conversionRate = BigDecimal.ZERO;

	public Summary(LocalDate date, BigDecimal gridImport, BigDecimal gridExport, BigDecimal consumption, BigDecimal production) {
		this.date = date;
		this.gridImport = gridImport == null ? BigDecimal.ZERO : gridImport;
		this.gridExport = gridExport == null ? BigDecimal.ZERO : gridExport;
		this.consumption = consumption == null ? BigDecimal.ZERO : consumption;
		this.production = production == null ? BigDecimal.ZERO : production;
	}

	public Summary(DailySummary daily, Total gridImport, Total gridExport, Total highestOutput, BigDecimal conversionRate) {
		this.date = daily.getDate();
		this.consumption = daily.getConsumption();
		this.production = daily.getProduction();
		this.gridImport = BigDecimal.valueOf(gridImport.getSummary());
		this.gridExport = BigDecimal.valueOf(gridExport.getSummary());
		this.highestOutput = highestOutput.getSummary();
		this.conversionRate = conversionRate;
	}

	public Summary(LocalDate date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Summary that) {
			if (this == o) return true;
			if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
			return Objects.equals(this.date, that.date);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
