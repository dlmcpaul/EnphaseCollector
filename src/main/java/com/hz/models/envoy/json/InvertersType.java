package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David on 22-Oct-17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class InvertersType extends TypeBase {

	private static final int MICRO_INVERTER = 1;
	private static final int BATTERY = 11;

	private List<Inverter> inverterList = new ArrayList<>();    // populated from api/v1/production/inverters

	public List<Inverter> getMicroInvertors() {
		return inverterList.stream().filter(inverter -> inverter.getDeviceType() == MICRO_INVERTER).collect(Collectors.toList());
	}

	public List<Inverter> getBatteries() {
		return inverterList.stream().filter(inverter -> inverter.getDeviceType() == BATTERY).collect(Collectors.toList());
	}

}
