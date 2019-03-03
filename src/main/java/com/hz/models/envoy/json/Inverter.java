package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by David on 22-Oct-17.
 * Need to process http://192.168.0.63/api/v1/production/inverters with
 * user envoy
 * password 010838
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Inverter {
    private String serialNumber;
    private Date lastReportDate;
    @JsonProperty(value="devType")
    private int deviceType;
    private int lastReportWatts;
    private int maxReportWatts;
}
