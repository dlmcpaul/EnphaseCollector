package com.hz.interfaces;

import com.hz.models.database.Summary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface SummaryRepository extends CrudRepository<Summary, LocalDate> {
	public List<Summary> findSummeriesByDateBetween(LocalDate from, LocalDate to);

	@Query(value = "select top 1 * from Summary order by date asc", nativeQuery = true)
	public Summary findFirst();
}
