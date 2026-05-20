package com.hz.utils;

import com.hz.interfaces.MetricCalculator;
import com.hz.metrics.Metric;
import com.hz.models.envoy.interfaces.Power;
import com.hz.models.envoy.json.*;
import com.hz.models.envoy.json.System;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2
public class MetricCalculatorStandard implements MetricCalculator {

	private static final String ENCHARGE = "ENCHARGE";

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

	private void calculateSavings(List<Metric> metricList, BigDecimal production, BigDecimal consumption, BigDecimal grid, BigDecimal batteryPower) {

		if (grid.compareTo(BigDecimal.ZERO) >= 0) {
			// Exporting
			metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, grid));
			metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, consumption));
			metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, 0));
		} else if (grid.compareTo(BigDecimal.ZERO) < 0) {
			// Importing
			metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, 0));
			metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, production));
			metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, grid.multiply(BigDecimal.valueOf(-1))));
		}
	}

	public List<Metric> calculateMetrics(System system) {
		ArrayList<Metric> metricList = new ArrayList<>();
		Optional<Power> productionMeter = system.getProduction().getProductionMeter();
		Optional<Power> totalConsumptionMeter = system.getProduction().getTotalConsumptionMeter();
		Optional<Power> netConsumptionMeter = system.getProduction().getNetConsumptionMeter();
		Optional<Power> storageMeter = system.getProduction().getStorageMeter();

		//log.info("production meter {} total consumption meter {} net consumption meter {} storage meter {}", productionMeter.isPresent(), totalConsumptionMeter.isPresent(), netConsumptionMeter.isPresent(), storageMeter.isPresent() );

		if (productionMeter.isPresent() == false) {
			log.error("No production meter found");
		} else {
			BigDecimal productionPower = BigDecimal.valueOf(Math.round(Math.max(system.getProduction().getProductionWatts().floatValue(), 0)));  // ignore negatives
			BigDecimal batteryPower;

			if (system.hasBattery()) {
				metricList.add(new Metric(Metric.METRIC_BATTERY_AVAILABLE, 1));
				batteryPower = BigDecimal.valueOf(calculateBatteryStats(metricList, system.getBatteries(), system.getInventoryList()));
			} else {
				metricList.add(new Metric(Metric.METRIC_BATTERY_AVAILABLE, 0));
				batteryPower = BigDecimal.ZERO;
			}
			metricList.add(new Metric(Metric.METRIC_PRODUCTION_CURRENT, productionPower, 5));
			metricList.add(new Metric(Metric.METRIC_PRODUCTION_VOLTAGE, system.getProduction().getProductionVoltage().floatValue()));

			metricList.add(new Metric(Metric.METRIC_PRODUCTION_TOTAL, BigDecimal.ZERO));
			metricList.add(new Metric(Metric.METRIC_CONSUMPTION_TOTAL, BigDecimal.ZERO));

			calculateFromMeters(metricList, Convertors.convertToLocalDateTime(productionMeter.get().getTimestamp()), totalConsumptionMeter, netConsumptionMeter, productionPower, batteryPower);

			Optional<Inverter> inverter = system.getProduction().getInverter();
			inverter.ifPresent(value -> log.info("Inverter: {} {} {}", value.getLastReportWatts(), value.getMaxReportWatts(), value.getDeviceType()));
		}

		system.getProduction().getMicroInvertersList().forEach(micro -> metricList.add(Metric.createPanelMetric(map(micro.getSerialNumber()), micro.getLastReportWatts(), 5)));

		return metricList;
	}

	// calculate the grid power from the meters.  Grid Power can be positive or negative depending on our solar or battery generation
	private void calculateGridPowerFromMeters(List<Metric> metricList, LocalDateTime captureTime, Optional<Power> totalConsumptionMeter, Optional<Power> netConsumptionMeter, BigDecimal productionPower, BigDecimal batteryPower) {
		BigDecimal gridPower;

		if (netConsumptionMeter.isPresent()) {
			gridPower = netConsumptionMeter.get().getActivePower();
		} else if (totalConsumptionMeter.isPresent()) {
			gridPower = totalConsumptionMeter.get().getActivePower();
		} else {
			log.error("Could not calculate Grid Power due to missing meter data");
			gridPower = BigDecimal.ZERO;
		}
		metricList.add(new Metric(Metric.METRIC_GRID_TOTAL, gridPower));
	}

	// calculate the household consumption power from the meters.  This should always be a positive number as the house is never generating power
	private void calculateConsumptionPowerFromMeters(List<Metric> metricList, LocalDateTime captureTime, Optional<Power> totalConsumptionMeter, Optional<Power> netConsumptionMeter, BigDecimal productionPower, BigDecimal batteryPower) {
		BigDecimal consumptionPower;
		if (netConsumptionMeter.isPresent()) {
			// Load + Solar
			consumptionPower = netConsumptionMeter.get().getActivePower().add(batteryPower).add(productionPower);
		} else if (totalConsumptionMeter.isPresent()) {
			// Load only
			consumptionPower = totalConsumptionMeter.get().getActivePower();
		} else {
			log.error("Could not calculate Consumption Power due to missing meter data");
			consumptionPower = BigDecimal.ZERO;
		}
		metricList.add(new Metric(Metric.METRIC_CONSUMPTION_CURRENT, consumptionPower.compareTo(BigDecimal.ZERO) > 0 ? consumptionPower : BigDecimal.ZERO));
	}

	private void calculateFromMeters(List<Metric> metricList, LocalDateTime captureTime, Optional<Power> totalConsumptionMeter, Optional<Power> netConsumptionMeter, BigDecimal productionPower, BigDecimal batteryPower) {
		BigDecimal consumptionPower;
		BigDecimal gridPower;

		if (totalConsumptionMeter.isPresent() && netConsumptionMeter.isPresent()) {
			consumptionPower = totalConsumptionMeter.get().getActivePower().add(batteryPower);
			gridPower = productionPower.add(batteryPower).subtract(consumptionPower).multiply(BigDecimal.valueOf(-1));
//			gridPower = netConsumptionMeter.get().getActivePower();
//			log.info("prod {} cons calc {} batt {} grid calc {} net cons {} tot cons {}",
//					productionPower,
//					consumptionPower,
//					batteryPower,
//					gridPower,
//					netConsumptionMeter.get().getActivePower(),
//					totalConsumptionMeter.get().getActivePower()
//			);
		} else if (totalConsumptionMeter.isPresent()) {
			consumptionPower = totalConsumptionMeter.get().getActivePower().add(batteryPower);
			gridPower = productionPower.add(batteryPower).subtract(consumptionPower).multiply(BigDecimal.valueOf(-1));
		} else if (netConsumptionMeter.isPresent()) {
			consumptionPower = netConsumptionMeter.get().getActivePower().add(batteryPower).add(productionPower);
			gridPower = netConsumptionMeter.get().getActivePower();
		} else {
			log.error("Missing Consumption Meter Readings");
			consumptionPower = BigDecimal.ZERO;
			gridPower = BigDecimal.ZERO;
		}
		metricList.add(new Metric(Metric.METRIC_CONSUMPTION_CURRENT, consumptionPower.compareTo(BigDecimal.ZERO) > 0 ? consumptionPower : BigDecimal.ZERO));
		metricList.add(new Metric(Metric.METRIC_GRID_TOTAL, gridPower));
//		log.info("time {} production {} consumption {} battery {} grid {}", captureTime, productionPower, consumptionPower, batteryPower, gridPower);

		calculateSavings(metricList, productionPower, consumptionPower, gridPower, batteryPower);
	}

	private BigDecimal calculateGrid(BigDecimal production, BigDecimal consumption, BigDecimal batteryPower) {
		return production.add(batteryPower).subtract(consumption).multiply(BigDecimal.valueOf(-1));
	}

	private BigDecimal calculateConsumption(BigDecimal production, Optional<Power> totalConsumptionMeter, Optional<Power> netConsumptionMeter, BigDecimal batteryPower) {
		if (totalConsumptionMeter.isPresent()) {
			log.info("total consumption {} battery {}", totalConsumptionMeter.get().getActivePower(), batteryPower);
			return totalConsumptionMeter.get().getActivePower().add(batteryPower);
		} else if (netConsumptionMeter.isPresent()) {
			log.info("net consumption/grid {} production {} battery {}", netConsumptionMeter.get().getActivePower(), production, batteryPower);
			return netConsumptionMeter.get().getActivePower().add(production).add(batteryPower);
		}

		log.error("No Consumption Meter found");
		return BigDecimal.ZERO;
	}

	private int calculateBatteryStats(List<Metric> metricList, List<BatteryDevice> batteryDeviceList, List<Inventory> inventoryList) {

		int totalBatteries = batteryDeviceList.size();
		int totalStateOfCharge = batteryDeviceList.stream().mapToInt(BatteryDevice::getStateOfCharge).sum() / totalBatteries;
		int totalBatteryPower = batteryDeviceList.stream().mapToInt(BatteryDevice::getRealPowerMW).sum();
		int totalBatteryCapacity = inventoryList.stream()
				.filter(inventory -> inventory.getType().equalsIgnoreCase(ENCHARGE))
				.flatMap(value -> value.getDeviceList().stream())
				.mapToInt(Device::getEnchargeCapacity)
				.sum();
		int maxTemperature = inventoryList.stream()
				.filter(inventory -> inventory.getType().equalsIgnoreCase(ENCHARGE))
				.flatMap(inventory -> inventory.getDeviceList().stream())
				.mapToInt(Device::getTemperatureAsInt)
				.max().orElse(0);

		int maxCellTemperature = inventoryList.stream()
				.filter(inventory -> inventory.getType().equalsIgnoreCase(ENCHARGE))
				.flatMap(inventory -> inventory.getDeviceList().stream())
				.mapToInt(Device::getMaxCellTempAsInt)
				.max().orElse(0);

		metricList.add(new Metric(Metric.METRIC_BATTERY_PERCENT, totalStateOfCharge));
		metricList.add(new Metric(Metric.METRIC_BATTERY_CAPACITY, totalBatteryCapacity));
		metricList.add(new Metric(Metric.METRIC_BATTERY_WATTS_REMAINING, totalBatteryCapacity * totalStateOfCharge / 100f));

		metricList.add(new Metric(Metric.METRIC_BATTERY_POWER, totalBatteryPower / 1000f));
		metricList.add(new Metric(Metric.METRIC_BATTERY_STATE, calculateChargeState((int) (totalBatteryPower / 1000f))));

		metricList.add(new Metric(Metric.METRIC_BATTERY_MAX_TEMPERATURE, maxTemperature));
		metricList.add(new Metric(Metric.METRIC_BATTERY_MAX_CELL_TEMPERATURE, maxCellTemperature));

		return (int) (totalBatteryPower / 1000f);
	}

	private int calculateChargeState(int totalBatteryPower) {
		// Treat IDLE as range -50 to +50
		if (totalBatteryPower >= -50 && totalBatteryPower <= 50) {
			return 0;
		}

		return Integer.compare(totalBatteryPower, 0);
	}

}
