package com.hz.interfaces;

import com.hz.models.database.Panel;
import com.hz.models.database.PanelSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface PanelRepository extends JpaRepository<Panel, Long> {
	@NativeQuery("""
SELECT Event.id as id, Event.time as time, Event.consumption as consumption, SUM(Panel.PANEL_VALUE) as production
 FROM Panel, EVENT_PANELS, Event
 where Panel.id = EVENT_PANELS.panels_id
 and EVENT_PANELS.event_id = Event.id
 and Event.time >= ?1
 group by Event.id, Event.time
 order by Event.time
""")
	List<PanelSummary> getPanelSummaries(LocalDateTime time);

	@NativeQuery("delete from Panel where Panel.id not in (select panels_id from EVENT_PANELS)")
	@Modifying
	void deletePanelsByTimeBefore(LocalDateTime time);

	@NativeQuery("delete from EVENT_PANELS where EVENT_PANELS.event_id in (select id from EVENT where Event.time < ?1)")
	@Modifying
	void deleteEventsPanelByTimeBefore(LocalDateTime time);

	@NativeQuery("select count(*) from EVENT_PANELS where EVENT_PANELS.event_id in (select id from EVENT where Event.time < ?1)")
	int countEventsPanelByTimeBefore(LocalDateTime time);

	@NativeQuery("select count(*) from Panel where Panel.id not in (select panels_id from EVENT_PANELS)")
	int countPanelsByTimeBefore(LocalDateTime time);

}
