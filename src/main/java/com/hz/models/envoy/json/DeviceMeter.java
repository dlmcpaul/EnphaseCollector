package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceMeter {
	private String eid;
	private String state;
	private String measurementType;
	private String phaseMode;
	private int phaseCount;
	private String meteringStatus;
	private List<String> statusFlags;

	public DeviceMeter() {
		this.phaseCount = 1;
	}
}
