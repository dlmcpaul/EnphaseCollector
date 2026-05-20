package com.hz.models.database;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class EventBase {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;

	private LocalDateTime time = LocalDateTime.now();
	private BigDecimal consumption = new BigDecimal(0);
	private BigDecimal production = new BigDecimal(0);
	private BigDecimal voltage = new BigDecimal(0);
	private BigDecimal grid = new BigDecimal(0);                    // Calculated Grid in/out
	private BigDecimal percentFull = new BigDecimal(0);             // Percentage of Battery (SOC)
	private String chargeState = "";                                    // CHARGE/DISCHARGE/IDLE
	private BigDecimal batteryPower = new BigDecimal(0);            // battery in/out
	private BigDecimal batteryWatts = new BigDecimal(0);            // Current Watts available
	private BigDecimal batteryCellTemperature = new BigDecimal(0);  // Highest Temperature of all Cell Packs
}