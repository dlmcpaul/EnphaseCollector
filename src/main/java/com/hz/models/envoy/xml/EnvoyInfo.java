package com.hz.models.envoy.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "envoy_info")
public class EnvoyInfo {
	@XmlElement
	public long time;
	@XmlElement(name="device")
	public EnvoyDevice envoyDevice;
	@XmlElement(name = "package")
	public List<EnvoyPackage> packages;

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
