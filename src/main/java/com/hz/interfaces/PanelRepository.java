package com.hz.interfaces;

import com.hz.models.database.Panel;
import com.hz.models.database.PanelSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PanelRepository extends JpaRepository<Panel, Long> {
	@Query(value = "SELECT Event.id as id, Event.time as time, Event.consumption as consumption, SUM(Panel.value) as production FROM Panel, EVENT_PANELS, Event where Panel.id = EVENT_PANELS.panels_id and EVENT_PANELS.event_id = Event.id " +
			"group by Event.id " +
			"order by id", nativeQuery = true)
	List<PanelSummary> getPanelSummaries();
}
