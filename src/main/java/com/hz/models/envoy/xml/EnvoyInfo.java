package com.hz.models.envoy.xml;

import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "envoy_info")
@NoArgsConstructor
public class EnvoyInfo {
	@XmlElement
	public long time;
	@XmlElement(name="device")
	public EnvoyDevice envoyDevice;
	@XmlElement(name = "package")
	public List<EnvoyPackage> packages;

	// Only needed for test bean
	public EnvoyInfo(String softwareVersion, String serialNumber) {
		this.envoyDevice = new EnvoyDevice();
		this.envoyDevice.software = softwareVersion;
		this.envoyDevice.sn = serialNumber;
	}

	public String getSoftwareVersion() {
		if (envoyDevice.software.isEmpty()) {
			return "Unknown";
		}
		return envoyDevice.software;
	}

	public String getSerialNumber() {
		if (envoyDevice.sn.isEmpty()) {
			return "000000000000";
		}
		return envoyDevice.sn;
	}

}
