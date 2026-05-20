package com.hz.interfaces;

import com.hz.models.database.EventSummary;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventSummaryRepository extends CrudRepository<EventSummary, Long> {
	List<EventSummary> findEventSummariesByTimeAfter(LocalDateTime time);
}
