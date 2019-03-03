package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/** 121703010838
 * Created by David on 22-Oct-17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {
    @JsonProperty(value="part_num")
    private String partNumber;
    @JsonProperty(value="installed")
    private Date installationDate;
    @JsonProperty(value="serial_num")
    private long serialNumber;
    @JsonProperty(value="device_status")
    private List<String> deviceStatus;
    @JsonProperty(value="last_rpt_date")
    private Date lastReportDate;
    @JsonProperty(value="admin_state")
    private int adminState;
    @JsonProperty(value="created_date")
    private Date creationDate;
    @JsonProperty(value="img_load_date")
    private Date imageLoadDate;
    @JsonProperty(value="img_pnum_running")
    private String imagePnumRunning;
    private String ptpn;
    @JsonProperty(value="producing")
    private boolean isProducing;
    @JsonProperty(value="communicating")
    private boolean isCommunicating;
    @JsonProperty(value="provisioned")
    private boolean isProvisioned;
    @JsonProperty(value="operating")
    private boolean isOperating;
    @JsonProperty(value="chaneid")
    private int channelId;
    @JsonProperty(value="device_control")
    private List<DeviceControl> deviceControlList;
}
