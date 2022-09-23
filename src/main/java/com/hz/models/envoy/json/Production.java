package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
	private static final String PRODUCTION_TYPE = "production";

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
	public List<Inverter> getMicroInvertersList() {
		return this.getInverter().orElseGet(InvertersType::new).getMicroInverters();
	}

	@JsonIgnore
	public List<Inverter> getBatteryList() {
		return this.getInverter().orElseGet(InvertersType::new).getBatteries();
	}

	@JsonIgnore
	public void setInverterList(List<Inverter> inverterList) {
		this.getInverter().ifPresent(invertersType -> invertersType.setInverterList(inverterList));
	}

	@JsonIgnore
	public Optional<InvertersType> getInverter() {
		return productionList.stream().filter(module -> module.getType().equalsIgnoreCase("inverters")).findFirst().map(InvertersType.class::cast);
	}

	@JsonIgnore
	public Optional<EimType> getProductionEim() {
		return findBymeasurementType(productionList, PRODUCTION_TYPE);
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
		return getDevice(PRODUCTION_TYPE).
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

	@JsonIgnore
	public Optional<DeviceMeter> getDevice(String measurementType) {
		return deviceMeterList.stream().filter(device -> device.getMeasurementType().compareToIgnoreCase(measurementType) == 0).findFirst();
	}

	@JsonIgnore
	public BigDecimal getPhaseCount() {
		return BigDecimal.valueOf(getDevice(PRODUCTION_TYPE).orElse(new DeviceMeter()).getPhaseCount());

	}

	@JsonIgnore
	public BigDecimal getProductionVoltage() {
		return getProductionMeter().orElse(new PowerMeter(BigDecimal.ZERO, getProductionEimVoltage())).getVoltage().divide(getPhaseCount(), 3, RoundingMode.HALF_UP);
	}

	@JsonIgnore
	public BigDecimal getProductionEimVoltage() {
		return getProductionEim().orElseGet(EimType::new).getRmsVoltage();
	}

	@JsonIgnore
	public BigDecimal getProductionWatts() {
		return getProductionMeter().orElse(new PowerMeter(getProductionEimWatts(), BigDecimal.ZERO)).getActivePower();
	}

	@JsonIgnore
	public BigDecimal getProductionEimWatts() {
		return getProductionEim().orElse(new EimType(getInverterWatts())).getWattsNow();
	}

	@JsonIgnore
	public BigDecimal getInverterWatts() {
		return getInverter().orElseGet(InvertersType::new).getWattsNow();
	}

	@JsonIgnore
	public BigDecimal getConsumptionWatts() {
		return getTotalConsumptionMeter().orElse(new PowerMeter(getConsumptionEimWatts(), BigDecimal.ZERO)).getActivePower();
	}

	@JsonIgnore
	public BigDecimal getConsumptionEimWatts() {
		return getTotalConsumptionEim().orElseGet(EimType::new).getWattsNow();
	}

	private Optional<EimType> findBymeasurementType(List<TypeBase> list, String measurementType) {
		return filterToEimType(list).stream()
				.filter(eim -> eim.getMeasurementType() == null || eim.getMeasurementType().equalsIgnoreCase(measurementType))
				.findFirst();
	}

	private List<EimType> filterToEimType(List<TypeBase> list) {
		return list == null ? new ArrayList<>() : list.stream()
				.filter(module -> module.getType().equalsIgnoreCase("eim"))
				.map(EimType.class::cast)
				.collect(Collectors.toList());
	}

}
