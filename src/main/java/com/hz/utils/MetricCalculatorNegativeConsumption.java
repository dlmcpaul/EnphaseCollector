package com.hz.utils;

import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.EimType;
import com.hz.models.envoy.json.InvertersType;
import com.hz.models.envoy.json.System;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// This metric calculator treats consumption as the grid import/export value
// negative consumption is grid export (solar excess) so real consumption = production - ABS(consumption)
// positive consumption is grid import so real consumption = production + consumption

@Log4j2
public class MetricCalculatorNegativeConsumption implements MetricCalculator {

	public MetricCalculatorNegativeConsumption() {
		log.info("Negative Consumption Metric Calculator Activated");
	}

	private void calculateSavings(ArrayList<Metric> metricList, BigDecimal production, BigDecimal consumption) {
		if (consumption.compareTo(BigDecimal.ZERO) < 0) {
			BigDecimal absConsumption = consumption.multiply(BigDecimal.valueOf(-1));

			metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, absConsumption, 0));
			metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, production, absConsumption));
			metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, 0));

			// Attempt to calculate consumption
			metricList.add(new Metric(Metric.METRIC_CONSUMPTION_CURRENT, production, absConsumption));
		} else {
			metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, 0));
			metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, production));
			metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, consumption));

			// Attempt to calculate consumption
			metricList.add(new Metric(Metric.METRIC_CONSUMPTION_CURRENT, production.add(consumption)));
		}
		metricList.add(new Metric( Metric.METRIC_SOLAR_DIFFERENCE, consumption));
	}

	public List<Metric> calculateMetrics(System system) {
		ArrayList<Metric> metricList = new ArrayList<>();

		BigDecimal production = system.getProduction().getProductionWatts();
		metricList.add(new Metric(Metric.METRIC_PRODUCTION_CURRENT, production, 5));
		metricList.add(new Metric(Metric.METRIC_PRODUCTION_VOLTAGE, system.getProduction().getProductionVoltage().floatValue()));

		Optional<EimType> productionEim = system.getProduction().getProductionEim();
		Optional<InvertersType> inverter = system.getProduction().getInverter();
		if (productionEim.isPresent() && inverter.isPresent()) {
			log.debug("production: eim time {} eim {} inverter time {} inverter {} calculated {}", Convertors.convertToLocalDateTime(productionEim.get().getReadingTime()), productionEim.get().getWattsNow(), Convertors.convertToLocalDateTime(inverter.get().getReadingTime()), inverter.get().getWattsNow(), production);
			metricList.add(new Metric(Metric.METRIC_PRODUCTION_TOTAL, inverter.get().getWattsLifetime()));
		}

		BigDecimal consumption = system.getProduction().getNetConsumptionWatts();

		Optional<EimType> consumptionEim = system.getProduction().getNetConsumptionEim();
		if (consumptionEim.isPresent()) {
			log.debug("consumption: eim time {} eim {} calculated {}", Convertors.convertToLocalDateTime(consumptionEim.get().getReadingTime()), consumptionEim.get().getWattsNow(), consumption);
			metricList.add(new Metric(Metric.METRIC_CONSUMPTION_TOTAL, consumptionEim.get().getWattsLifetime()));
		}

		calculateSavings(metricList, production, consumption);

		system.getProduction().getMicroInvertersList().forEach(micro -> metricList.add(Metric.createPanelMetric(micro.getSerialNumber(), micro.getLastReportWatts(), 5)));

		return metricList;
	}
}
