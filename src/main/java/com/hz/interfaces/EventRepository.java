package com.hz.interfaces;

import com.hz.models.database.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {
	public Event findTopByTime(LocalDateTime time);
	public List<Event> findEventsByTimeAfter(LocalDateTime time);

	@Query(value = "select nvl(sum(production - consumption), 0) from Event where time > ?1 and (production - consumption) > 0", nativeQuery = true)
	public Long findExcessProductionAfter(LocalDateTime time);

	@Query(value = "select nvl(sum(consumption - production), 0) from Event where time > ?1 and (consumption - production) > 0", nativeQuery = true)
	public Long findExcessConsumptionAfter(LocalDateTime time);

	@Query(value = "select nvl(sum(production),0) from Event where time > ?1", nativeQuery = true)
	public Long findTotalProductionAfter(LocalDateTime time);

	@Query(value = "select nvl(sum(consumption),0) from Event where time > ?1", nativeQuery = true)
	public Long findTotalConsumptionAfter(LocalDateTime time);

	@Query(value = "select nvl(max(production),0) from Event where time > ?1", nativeQuery = true)
	public Long findMaxProductionAfter(LocalDateTime time);
}
