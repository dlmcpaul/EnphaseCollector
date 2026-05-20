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

    private int percentFull;
    @JsonProperty(value="real_power_w")
    private int realPowerW;
    @JsonProperty(value="encharge_capacity")
    private int enchargeCapacity;
    // temperature value can be returned as "unknown" so need to treat as string and convert.
    private String temperature;
    private String maxCellTemp;

    @JsonProperty(value="Enpwr_grid_mode")
    private String enpowerGridMode;
    @JsonProperty(value="Enchg_grid_mode ")
    private String enchargeGridMode;

    @JsonProperty(value="device_control")
    private List<DeviceControl> deviceControlList;

    public int getMaxCellTempAsInt() {
        try {
            return Integer.parseInt(maxCellTemp);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getTemperatureAsInt() {
        try {
            return Integer.parseInt(temperature);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
