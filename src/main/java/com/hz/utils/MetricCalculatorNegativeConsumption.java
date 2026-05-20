package com.hz.utils;

import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.interfaces.Power;
import com.hz.models.envoy.json.Inverter;
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

		Optional<Power> productionPower = system.getProduction().getProductionMeter();
		Optional<Inverter> inverter = system.getProduction().getInverter();
		if (productionPower.isPresent() && inverter.isPresent()) {
			log.debug("production: eim time {} eim {} inverter time {} inverter {} calculated {}", Convertors.convertToLocalDateTime(productionPower.get().getTimestamp()), productionPower.get().getActivePower(), Convertors.convertToLocalDateTime(inverter.get().getLastReportDate().getTime()), inverter.get().getLastReportWatts(), production);
			metricList.add(new Metric(Metric.METRIC_PRODUCTION_TOTAL, BigDecimal.ZERO));
		}

		BigDecimal consumption = system.getProduction().getNetConsumptionWatts();

		Optional<Power> consumptionMeter = system.getProduction().getNetConsumptionMeter();
		if (consumptionMeter.isPresent()) {
			log.debug("consumption: eim time {} eim {} calculated {}", Convertors.convertToLocalDateTime(consumptionMeter.get().getTimestamp()), consumptionMeter.get().getActivePower(), consumption);
			metricList.add(new Metric(Metric.METRIC_CONSUMPTION_TOTAL, BigDecimal.ZERO));
		}

		calculateSavings(metricList, production, consumption);

		system.getProduction().getMicroInvertersList().forEach(micro -> metricList.add(Metric.createPanelMetric(micro.getSerialNumber(), micro.getLastReportWatts(), 5)));

		return metricList;
	}
}
