package com.hz.interfaces;

import com.hz.models.database.ElectricityRate;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ElectricityRateRepository extends CrudRepository<ElectricityRate, LocalDate> {
	public Optional<ElectricityRate> findFirstByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate effectiveDate);
}
