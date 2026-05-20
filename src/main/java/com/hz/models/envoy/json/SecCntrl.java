package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecCntrl {
	@JsonProperty(value="ENC_agg_soc")
	private int totalStateOfCharge;
	@JsonProperty(value="ENC_agg_backup_energy")
	private int backupEnergyW;
	@JsonProperty(value="ENC_agg_avail_energy")
	private int currentEnergyW;
	@JsonProperty(value="Enc_max_available_capacity")
	private int maxEnergyW;

	// Calculated Values
	private int lastEnergyW;
}
