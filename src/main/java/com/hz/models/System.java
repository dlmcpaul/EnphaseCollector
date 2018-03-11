package com.hz.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by David on 22-Oct-17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class System {
//    {"software_build_epoch":1490713682,"is_nonvoy":false,"db_size":"15 MB","db_percent_full":"4","timezone":"Australia/Sydney","current_date":"10/22/2017","current_time":"12:01","network":{"web_comm":true,"ever_reported_to_enlighten":true,"last_enlighten_report_time":1508633985,"primary_interface":"wlan0","interfaces":[{"type":"ethernet","interface":"eth0","mac":"00:1D:C0:68:12:36","dhcp":true,"ip":"169.254.120.1","signal_strength":0,"signal_strength_max":1,"carrier":false},{"signal_strength":1,"signal_strength_max":5,"type":"wifi","interface":"wlan0","mac":"D4:F5:13:FE:08:25","dhcp":true,"ip":"192.168.0.63","carrier":true,"supported":true,"present":true,"configured":true,"status":"connected"}]},"tariff_set":false,"comm":{"num":16,"level":5,"pcu":{"num":16,"level":5},"acb":{"num":0,"level":0}},"alerts":[],"update_status":"satisfied"}
    @JsonProperty(value="software_build_epoch")
    private Date softwareBuildEpoch;
    @JsonProperty(value="is_nonvoy")
    private boolean isNonvoy;
    @JsonProperty(value="db_size")
    private String databaseSize;
    @JsonProperty(value="db_percent_full")
    private int databasePercFull;
    private String timezone;
    @JsonProperty(value="current_date")
    private String currentDate;
    @JsonProperty(value="current_time")
    private String currentTime;
    private Comm comm;
    @JsonProperty(value="alerts")
    private List<String> alertList;
    @JsonProperty(value="update_status")
    private String updateStatus;

    private Production production;  // Populated from production.json
    private List<Inventory> inventoryList;  // populated from inventory.json
}
