package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hz.models.envoy.interfaces.Power;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by David on 23-Oct-17.
 */
@Data
@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public class Production {
	private static final String PRODUCTION_TYPE = "production";
	private static final String NET_CONSUMPTION_TYPE = "net-consumption";
	private static final String TOTAL_CONSUMPTION_TYPE = "total-consumption";
	private static final String STORAGE_TYPE = "storage";

	private static final int MICRO_INVERTER = 1;
	private static final int BATTERY = 11;

	@JsonProperty(value="production")
	private List<TypeBase> productionList;
	@JsonProperty(value="consumption")
	private List<TypeBase> consumptionList;

	@JsonIgnore
	private List<Inverter> inverterList;
	@JsonIgnore
	private List<PowerMeter> powerMeterList;
	@JsonIgnore
	private List<DeviceMeter> deviceMeterList;

	@JsonIgnore
	private List<MeterReport> meterReportList;

	@JsonIgnore
	public List<Inverter> getMicroInvertersList() {
		return inverterList.stream().filter(inverter -> inverter.getDeviceType() == MICRO_INVERTER).toList();
	}

	@JsonIgnore
	public List<Inverter> getBatteryList() {
		return inverterList.stream().filter(inverter -> inverter.getDeviceType() == BATTERY).toList();
	}

	@JsonIgnore
	public void setInverterList(List<Inverter> inverterList) {
		this.inverterList = inverterList;
	}

	@JsonIgnore
	public Optional<Inverter> getInverter() {
		return inverterList.stream().filter(inverter -> inverter.getDeviceType() != MICRO_INVERTER && inverter.getDeviceType() != BATTERY).findFirst();
	}

	@JsonIgnore
	public Optional<Power> getProductionMeter() {
		Optional<Power> result = getMeterReport(PRODUCTION_TYPE).flatMap(Optional::of);
		if (result.isPresent()) {
			return result;
		}
		if (deviceMeterList == null || deviceMeterList.isEmpty()) {
			return getProductionAsPowerMeter().flatMap(Optional::of);
		} else {
			return getDevice(PRODUCTION_TYPE).
					flatMap(device -> getPowerMeter(device.getEid()));
		}
	}

	private Optional<PowerMeter> getProductionAsPowerMeter() {
		if (productionList == null) {
			return Optional.empty();
		}

		if (productionList.size() == 1) {
			// Envoy S with only a InvertersType object
			return Optional.of(new PowerMeter(productionList.getFirst().getWattsNow(), BigDecimal.ZERO));
		}

		return productionList.stream()
				.filter(typeBase -> typeBase instanceof EimType)
				.map(typeBase -> (EimType) typeBase)
				.map(eimType -> new PowerMeter(eimType.getWattsNow(), BigDecimal.ZERO, eimType.getReadingTime()))
				.findFirst();
	}

	private Optional<PowerMeter> getConsumptionAsPowerMeter(String consumptionType) {
		if (consumptionList == null) {
			// Envoy S has no consumption inputs
			return Optional.of(new PowerMeter(BigDecimal.ZERO, BigDecimal.ZERO));
		}

		if (consumptionList.size() > 1) {
			return consumptionList.stream()
					.filter(typeBase -> typeBase instanceof EimType)
					.map(typeBase -> (EimType) typeBase)
					.filter(typeBase -> typeBase.getMeasurementType().equals(consumptionType))
					.map(eimType -> new PowerMeter(eimType.getWattsNow(), BigDecimal.ZERO, eimType.getReadingTime()))
					.findFirst();
		}

		return consumptionList.stream()
				.filter(typeBase -> typeBase instanceof EimType)
				.map(typeBase -> (EimType) typeBase)
				.map(eimType -> new PowerMeter(eimType.getWattsNow(), BigDecimal.ZERO, eimType.getReadingTime()))
				.findFirst();
	}

	@JsonIgnore
	public Optional<Power> getNetConsumptionMeter() {
		Optional<Power> result = getMeterReport(NET_CONSUMPTION_TYPE).flatMap(Optional::of);
		if (result.isPresent()) {
			return result;
		}
		if (deviceMeterList == null || deviceMeterList.isEmpty()) {
			return getConsumptionAsPowerMeter(NET_CONSUMPTION_TYPE).flatMap(Optional::of);
		} else {
			return getDevice(NET_CONSUMPTION_TYPE).
					flatMap(device -> getPowerMeter(device.getEid()));
		}
	}

	@JsonIgnore
	public Optional<Power> getTotalConsumptionMeter() {
		Optional<Power> result = getMeterReport(TOTAL_CONSUMPTION_TYPE).flatMap(Optional::of);
		if (result.isPresent()) {
			return result;
		}
		if (deviceMeterList == null || deviceMeterList.isEmpty()) {
			return getConsumptionAsPowerMeter(TOTAL_CONSUMPTION_TYPE).flatMap(Optional::of);
		} else {
			return getDevice(TOTAL_CONSUMPTION_TYPE).
					flatMap(device -> getPowerMeter(device.getEid()));
		}
	}

	@JsonIgnore
	public Optional<Power> getStorageMeter() {
		return getMeterReport(STORAGE_TYPE).flatMap(Optional::of);
	}

	@JsonIgnore
	public BigDecimal getPhaseCount() {
		return getMeterReport(PRODUCTION_TYPE)
				.map(report -> BigDecimal.valueOf(report.getPhaseCount()))
				.orElseGet(() -> getDevice(PRODUCTION_TYPE)
						.map(meter -> BigDecimal.valueOf(meter.getPhaseCount()))
						.orElse(BigDecimal.ONE));

	}

	@JsonIgnore
	public BigDecimal getProductionVoltage() {
		return getProductionMeter().orElse(new PowerMeter(BigDecimal.ZERO, BigDecimal.ZERO)).getVoltage().divide(getPhaseCount(), 3, RoundingMode.HALF_UP);
	}

	@JsonIgnore
	public BigDecimal getProductionWatts() {
		return getProductionMeter().orElse(new PowerMeter(BigDecimal.ZERO, BigDecimal.ZERO)).getActivePower();
	}

	@JsonIgnore
	public BigDecimal getInverterWatts() {
		return BigDecimal.valueOf(getInverter().orElseGet(Inverter::new).getLastReportWatts());
	}

	@JsonIgnore
	public BigDecimal getConsumptionWatts() {
		return getTotalConsumptionMeter().orElse(new PowerMeter(BigDecimal.ZERO, BigDecimal.ZERO)).getActivePower();
	}

	@JsonIgnore
	public BigDecimal getNetConsumptionWatts() {
		return getNetConsumptionMeter().orElse(new PowerMeter(BigDecimal.ZERO, BigDecimal.ZERO)).getActivePower();
	}

	private Optional<MeterReport> getMeterReport(String measurementType) {
		if (meterReportList != null) {
			return meterReportList.stream().filter(meterReport -> meterReport.getReportType().compareToIgnoreCase(measurementType) == 0).findFirst();
		}
		return Optional.empty();
	}

	private Optional<DeviceMeter> getDevice(String measurementType) {
		return deviceMeterList.stream().filter(device -> device.getMeasurementType().compareToIgnoreCase(measurementType) == 0).findFirst();
	}

	private Optional<PowerMeter> getPowerMeter(String eid) {
		return powerMeterList.stream().filter(power -> power.getEid().compareToIgnoreCase(eid) == 0).findFirst();
	}

	private Optional<EimType> findByMeasurementType(List<TypeBase> list, String measurementType) {
		return filterToEimType(list).stream()
				.filter(eim -> eim.getMeasurementType() == null || eim.getMeasurementType().equalsIgnoreCase(measurementType))
				.findFirst();
	}

	private List<EimType> filterToEimType(List<TypeBase> list) {
		return list == null ? new ArrayList<>() : list.stream()
				.filter(module -> module.getType().equalsIgnoreCase("eim"))
				.map(EimType.class::cast)
				.toList();
	}

}
