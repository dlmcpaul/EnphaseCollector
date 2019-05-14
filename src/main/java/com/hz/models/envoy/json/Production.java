package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by David on 23-Oct-17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Production {
	@JsonProperty(value="production")
	private List<TypeBase> productionList;
	@JsonProperty(value="consumption")
	private List<TypeBase> consumptionList;
	@JsonProperty(value="storage")
	private List<TypeBase> storageList;

	@JsonIgnore
	public List<Inverter> getMicroInvertorsList() {
		Optional<TypeBase> inverter = this.getInverter();
		if (inverter.isPresent()) {
			return ((InvertersType)inverter.get()).getMicroInvertors();
		}

		return new ArrayList<>();
	}

	@JsonIgnore
	public List<Inverter> getBatteryList() {
		Optional<TypeBase> inverter = this.getInverter();
		if (inverter.isPresent()) {
			return ((InvertersType)inverter.get()).getBatteries();
		}

		return new ArrayList<>();
	}

	@JsonIgnore
	public void setInverterList(List<Inverter> inverterList) {
		Optional<TypeBase> inverter = this.getInverter();
		if (inverter.isPresent()) {
			((InvertersType)inverter.get()).setInverterList(inverterList);
		}
	}

	@JsonIgnore
	public Optional<TypeBase> getInverter() {
		return productionList.stream().filter(module -> module.getType().equalsIgnoreCase("inverters")).findFirst();
	}

	@JsonIgnore
	public Optional<EimType> getProductionEim() {
		return findBymeasurementType(productionList, "production");
	}

	@JsonIgnore
	public Optional<EimType> getConsumptionEim() {
		return findBymeasurementType(consumptionList, "total-consumption");
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
