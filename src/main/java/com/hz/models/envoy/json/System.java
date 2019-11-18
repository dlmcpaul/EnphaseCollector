package com.hz.models.envoy.json;

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
    @JsonProperty(value="software_build_epoch")
    private long softwareBuildEpoch;
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
    private Network network;
    private Comm comm;
    //@JsonProperty(value="alerts")  This looks to be problematic.  The subtype is probably not string
    //private List<String> alertList;
    @JsonProperty(value="update_status")
    private String updateStatus;

    public Date getSoftwareBuildEpoch() {
        return new Date(softwareBuildEpoch * 1000L);
    }

    private Production production;  // Populated from production.json
    private List<Inventory> inventoryList;  // populated from inventory.json
    private Wireless wireless;  // populated from wireless_display.json
}
