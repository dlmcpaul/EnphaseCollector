package com.hz.interfaces;

import com.hz.models.database.Summary;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface SummaryRepository extends CrudRepository<Summary, Long> {
	public List<Summary> findSummeriesByDateBetween(LocalDate from, LocalDate to);
}
