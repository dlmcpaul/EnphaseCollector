package com.hz.utils;

import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.AcbType;
import com.hz.models.envoy.json.EimType;
import com.hz.models.envoy.json.InvertersType;
import com.hz.models.envoy.json.System;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2
public class MetricCalculatorStandard implements MetricCalculator {

	public MetricCalculatorStandard() {
		log.info("Standard Consumption Metric Calculator Activated");
	}

	// Table of my serial numbers to map to simpler values
	private final List<String> mySerialNumbers = Arrays.asList(
			"121707050571",
			"121707050096",
			"121707049853",
			"121707047544",
			"121707049848",
			"121707050094",
			"121707050367",
			"121707040461",
			"121707040638",
			"121707050013",
			"121707049878",
			"121707050549",
			"121707049876",
			"121707050098",
			"121707049864",
			"121707050570");

	private String map(String serial) {
		//  X X
		//  X X
		//    11
		//    12
		//  1 2 3
		//
		//         4
		//         5
		//         6
		//         7
		//
		//         8
		//         9
		//         10

		if (mySerialNumbers.contains(serial)) {
			return String.valueOf(mySerialNumbers.indexOf(serial) + 1);
		}

		return serial;
	}

	private void calculateSavings(ArrayList<Metric> metricList, BigDecimal production, BigDecimal consumption) {
		if (consumption.compareTo(BigDecimal.ZERO) > 0) {
			if (production.compareTo(consumption) > 0) {
				metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, production, consumption));
				metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, consumption));
				metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, 0));
			} else {
				metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, 0));
				metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, production));
				metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, consumption, production));
			}
		} else {
			// No consumption available so zero these metrics as we cannot calculate them
			metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, 0));
			metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, 0));
			metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, 0));
		}
		metricList.add(new Metric( Metric.METRIC_SOLAR_DIFFERENCE, production, consumption));
	}

	public List<Metric> calculateMetrics(System system) {
		ArrayList<Metric> metricList = new ArrayList<>();

		BigDecimal production = system.getProduction().getProductionWatts();
		BigDecimal consumption = system.getProduction().getConsumptionWatts();

		metricList.add(new Metric(Metric.METRIC_PRODUCTION_CURRENT, production, 5));
		metricList.add(new Metric(Metric.METRIC_CONSUMPTION_CURRENT, consumption));
		metricList.add(new Metric(Metric.METRIC_PRODUCTION_VOLTAGE, system.getProduction().getProductionVoltage().floatValue()));

		Optional<EimType> productionEim = system.getProduction().getProductionEim();
		Optional<InvertersType> inverter = system.getProduction().getInverter();
		if (productionEim.isPresent() && inverter.isPresent()) {
			log.debug("production: eim time {} eim {} inverter time {} inverter {} calculated {}", Convertors.convertToLocalDateTime(productionEim.get().getReadingTime()), productionEim.get().getWattsNow(), Convertors.convertToLocalDateTime(inverter.get().getReadingTime()), inverter.get().getWattsNow(), production);
			metricList.add(new Metric(Metric.METRIC_PRODUCTION_TOTAL, inverter.get().getWattsLifetime()));
		}

		Optional<EimType> consumptionEim = system.getProduction().getTotalConsumptionEim();
		if (consumptionEim.isPresent()) {
			log.debug("consumption: eim time {} eim {} calculated {}", Convertors.convertToLocalDateTime(consumptionEim.get().getReadingTime()), consumptionEim.get().getWattsNow(), consumption);
			metricList.add(new Metric(Metric.METRIC_CONSUMPTION_TOTAL, consumptionEim.get().getWattsLifetime()));
		}

		calculateSavings(metricList, production, consumption);

		// log battery data
		// First battery inverters
		system.getProduction().getBatteryList().forEach(battery -> log.info("Battery Last {} Max {}", battery.getLastReportWatts(), battery.getMaxReportWatts()));

		// then battery storage
		if (system.getProduction().getStorageList() != null) {
			system.getProduction().getStorageList().stream()
					.filter(storage -> storage.getType().equalsIgnoreCase("acb"))
					.map(AcbType.class::cast)
					.forEach(storage -> log.info("Storage #{} state {} perc full {} Watts {}", storage.getActiveCount(), storage.getState(), storage.getPercentFull(), storage.getWattsNow()));
		}
		system.getProduction().getMicroInvertersList().forEach(micro -> metricList.add(Metric.createPanelMetric(map(micro.getSerialNumber()), micro.getLastReportWatts(), 5)));

		return metricList;
	}
}
