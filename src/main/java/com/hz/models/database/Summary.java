package com.hz.models.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Summary {

	@Id
	private LocalDate date;
	private BigDecimal gridImport = BigDecimal.ZERO;
	private BigDecimal gridExport = BigDecimal.ZERO;
	private BigDecimal consumption = BigDecimal.ZERO;
	private BigDecimal production = BigDecimal.ZERO;
	private Long highestOutput = 0L;

	public Summary(LocalDate date, BigDecimal gridImport, BigDecimal gridExport, BigDecimal consumption, BigDecimal production) {
		this.date = date;
		this.gridImport = gridImport == null ? BigDecimal.ZERO : gridImport;
		this.gridExport = gridExport == null ? BigDecimal.ZERO : gridExport;
		this.consumption = consumption == null ? BigDecimal.ZERO : consumption;
		this.production = production == null ? BigDecimal.ZERO : production;
	}

	public Summary(DailySummary daily, Total gridImport, Total gridExport, Total highestOutput) {
		this.date = daily.getDate();
		this.consumption = daily.getConsumption();
		this.production = daily.getProduction();
		this.gridImport = BigDecimal.valueOf(gridImport.getValue());
		this.gridExport = BigDecimal.valueOf(gridExport.getValue());
		this.highestOutput = highestOutput.getValue();
	}
}
