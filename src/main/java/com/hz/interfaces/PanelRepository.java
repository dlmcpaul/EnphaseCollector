package com.hz.interfaces;

import com.hz.models.database.Panel;
import com.hz.models.database.PanelSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PanelRepository extends JpaRepository<Panel, Long> {
	@Query(value = "SELECT Event.id as id, Event.time as time, Event.consumption as consumption, SUM(Panel.value) as production FROM Panel, EVENT_PANELS, Event where Panel.id = EVENT_PANELS.panels_id and EVENT_PANELS.event_id = Event.id " +
			"and Event.time >= ?1 " +
			"group by Event.id " +
			"order by id", nativeQuery = true)
	List<PanelSummary> getPanelSummaries(LocalDateTime time);

	@Query(value="delete from Panel where Panel.id not in (select panels_id from EVENT_PANELS)", nativeQuery=true)
	@Modifying
	void deletePanelsByTimeBefore(LocalDateTime time);

	@Query(value="delete from EVENT_PANELS where EVENT_PANELS.event_id in (select id from EVENT where Event.time < ?1)", nativeQuery=true)
	@Modifying
	void deleteEventsPanelByTimeBefore(LocalDateTime time);

	@Query(value="select count(*) from EVENT_PANELS where EVENT_PANELS.event_id in (select id from EVENT where Event.time < ?1)", nativeQuery=true)
	int countEventsPanelByTimeBefore(LocalDateTime time);

	@Query(value="select count(*) from Panel where Panel.id not in (select panels_id from EVENT_PANELS)", nativeQuery=true)
	int countPanelsByTimeBefore(LocalDateTime time);

}
