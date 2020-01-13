package com.hz.interfaces;

import com.hz.models.database.ElectricityRate;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface ElectricityRateRepository extends CrudRepository<ElectricityRate, LocalDate> {
	public ElectricityRate findFirstByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate effectiveDate);
}
