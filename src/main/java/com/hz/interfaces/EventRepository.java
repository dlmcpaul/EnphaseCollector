package com.hz.interfaces;

import com.hz.models.database.DailySummary;
import com.hz.models.database.Event;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {
	Event findTopByTime(LocalDateTime time);

	@NativeQuery("select COALESCE(sum(Event.production),0) from Event where Event.time > ?1")
	Long findTotalProductionAfter(LocalDateTime time);

	@NativeQuery("select COALESCE(sum(Event.consumption),0) from Event where Event.time > ?1")
	Long findTotalConsumptionAfter(LocalDateTime time);

	@NativeQuery("select COALESCE(max(Event.production),0) from Event where Event.time > ?1")
	Long findMaxProductionAfter(LocalDateTime time);

	@NativeQuery("select COALESCE(sum(Event.grid), 0) from Event where Event.time > ?1 and Event.grid > 0")
	Long findGridImportAfter(LocalDateTime time);

	@NativeQuery("select COALESCE(sum(Event.grid), 0) * -1 from Event where Event.time > ?1 and Event.grid < 0")
	Long findGridExportAfter(LocalDateTime time);

	@NativeQuery("select COALESCE(sum(Event.battery_power), 0) * -1 from Event where Event.time > ?1 and Event.battery_power < 0")
	Long findChargedAfter(LocalDateTime time);

	@NativeQuery("select COALESCE(sum(Event.battery_power), 0) from Event where Event.time > ?1 and Event.battery_power > 0")
	Long findDischargedAfter(LocalDateTime time);

	@NativeQuery("""
select cast(Event.time as date) as eachDay,
       COALESCE(sum(Event.production),0) as production,
       COALESCE(sum(Event.consumption),0) as consumption,
       SUM(CASE
           WHEN Event.battery_power <= 0 THEN Event.battery_power * -1
           ELSE 0 END) as batteryCharged,
       SUM(CASE
           WHEN Event.battery_power >= 0 THEN Event.battery_power
           ELSE 0 END) as batteryDischarged,
       SUM(CASE
           WHEN Event.grid >= 0 THEN Event.grid
           ELSE 0 END) as gridImport,
       SUM(CASE
           WHEN Event.grid <= 0 THEN Event.grid * -1
           ELSE 0 END) as gridExport,
       MAX(Event.production) as highestProduction,
       MIN(Event.percent_Full) as MinBatterySoc,
       MAX(Event.percent_Full) as MaxBatterySoc,
       MAX(Event.battery_cell_temperature) as maxCellTemperature,
       (select top 1 Event.percent_Full from Event where Event.time < ?1 order by Event.time desc) as currentBatterySoc
from Event where Event.time < ?1 group by eachDay order by eachDay
""")
	List<DailySummary> findAllBefore(LocalDateTime time);

	@NativeQuery("delete from Event where Event.time < ?1")
	@Modifying
	void deleteEventsByTimeBefore(LocalDateTime time);
}
