package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by David on 23-Oct-17.
 */
@Data
@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public class Production {
	@JsonProperty(value="production")
	private List<TypeBase> productionList;
	@JsonProperty(value="consumption")
	private List<TypeBase> consumptionList;
	@JsonProperty(value="storage")
	private List<TypeBase> storageList;

	@JsonIgnore
	private List<PowerMeter> powerMeterList;
	@JsonIgnore
	private List<DeviceMeter> deviceMeterList;

	@JsonIgnore
	public List<Inverter> getMicroInvertorsList() {
		Optional<InvertersType> inverter = this.getInverter();
		if (inverter.isPresent()) {
			return inverter.get().getMicroInvertors();
		}

		return new ArrayList<>();
	}

	@JsonIgnore
	public List<Inverter> getBatteryList() {
		Optional<InvertersType> inverter = this.getInverter();
		if (inverter.isPresent()) {
			return inverter.get().getBatteries();
		}

		return new ArrayList<>();
	}

	@JsonIgnore
	public void setInverterList(List<Inverter> inverterList) {
		Optional<InvertersType> inverter = this.getInverter();
		if (inverter.isPresent()) {
			inverter.get().setInverterList(inverterList);
		}
	}

	@JsonIgnore
	public Optional<InvertersType> getInverter() {
		return productionList.stream().filter(module -> module.getType().equalsIgnoreCase("inverters")).findFirst().map(obj -> (InvertersType) obj);
	}

	@JsonIgnore
	public Optional<EimType> getProductionEim() {
		return findBymeasurementType(productionList, "production");
	}

	@JsonIgnore
	public Optional<EimType> getTotalConsumptionEim() {
		return findBymeasurementType(consumptionList, "total-consumption");
	}

	@JsonIgnore
	public Optional<EimType> getNetConsumptionEim() {
		return findBymeasurementType(consumptionList, "net-consumption");
	}

	private Optional<PowerMeter> getProductionMeter() {
		return getDevice("production").
				flatMap(device -> getPowerMeter(device.getEid()));
	}

	private Optional<PowerMeter> getNetConsumptionMeter() {
		return getDevice("net-consumption").
				flatMap(device -> getPowerMeter(device.getEid()));
	}

	private Optional<PowerMeter> getTotalConsumptionMeter() {
		return getDevice("total-consumption").
				flatMap(device -> getPowerMeter(device.getEid()));
	}

	private Optional<PowerMeter> getPowerMeter(String eid) {
		return powerMeterList.stream().filter(power -> power.getEid().compareToIgnoreCase(eid) == 0).findFirst();
	}

	private Optional<DeviceMeter> getDevice(String measurementType) {
		return deviceMeterList.stream().filter(device -> device.getMeasurementType().compareToIgnoreCase(measurementType) == 0).findFirst();
	}

	@JsonIgnore
	public BigDecimal getProductionWatts() {

		if (getProductionMeter().isPresent()) {
			return getProductionMeter().get().getActivePower();
		}

		if (getProductionEim().isPresent()) {
			return getProductionEim().get().getWattsNow();
		}

		return getInverter().get().getWattsNow();
	}

	@JsonIgnore
	public BigDecimal getConsumptionWatts() {
		if (getTotalConsumptionMeter().isPresent()) {
			return getTotalConsumptionMeter().get().getActivePower();
		}

		return getTotalConsumptionEim().get().getWattsNow();
	}

	private Optional<EimType> findBymeasurementType(List<TypeBase> list, String measurementType) {
		return filterToEimType(list).stream()
				.filter(eim -> eim.getMeasurementType() == null || eim.getMeasurementType().equalsIgnoreCase(measurementType))
				.findFirst();
	}

	private List<EimType> filterToEimType(List<TypeBase> list) {
		return list.stream()
				.filter(module -> module.getType().equalsIgnoreCase("eim"))
				.map(obj -> (EimType) obj)
				.collect(Collectors.toList());
	}

}
