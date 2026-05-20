package com.hz.interfaces;

import com.hz.models.database.Summary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SummaryRepository extends CrudRepository<Summary, LocalDate> {
	List<Summary> findSummariesByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

	@NativeQuery("select top 1 * from Summary order by date asc")
	Summary findFirst();

	@NativeQuery("update summary set conversion_rate = ?1 where conversion_rate is null")
	@Modifying
	void updateAllSummariesWithConversion(BigDecimal conversionRate);
}
