package com.hz.interfaces;

import com.hz.models.database.Summary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SummaryRepository extends CrudRepository<Summary, LocalDate> {
	List<Summary> findSummariesByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

	@Query(value = "select top 1 * from Summary order by date asc", nativeQuery = true)
	Summary findFirst();

	@Query(value = "update summary set conversion_rate = ?1 where conversion_rate is null", nativeQuery = true)
	@Modifying
	void updateAllSummariesWithConversion(BigDecimal conversionRate);
}
