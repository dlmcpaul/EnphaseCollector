package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeviceInfo {
	private String vendor;
	private String device;
	@JsonProperty(value="vendor_id")
	private String vendorId;
	@JsonProperty(value="device_id")
	private String deviceId;
	private String manufacturer;
	private String model;
	private String serial;
	@JsonProperty(value="hw_version")
	private String hwVersion;
	@JsonProperty(value="usb_spec")
	private String usbSpec;
	@JsonProperty(value="usb_slot")
	private String usbSlot;
	private String driver;
	private String mac;
}
