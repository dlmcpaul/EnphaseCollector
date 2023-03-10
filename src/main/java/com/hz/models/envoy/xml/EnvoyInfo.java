package com.hz.models.envoy.xml;

import com.hz.utils.Convertors;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
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
	@XmlElement(name="build_info", nillable = true)
	public BuildInfo buildInfo;
	@XmlElement(name="web-tokens")
	public boolean webTokens;

	// Only needed for test bean
	public EnvoyInfo(String softwareVersion, String serialNumber) {
		this.envoyDevice = new EnvoyDevice();
		this.envoyDevice.software = softwareVersion;
		this.envoyDevice.sn = serialNumber;
	}

	public boolean isV7orAbove() {
		if (envoyDevice.software.isEmpty()) {
			return false;
		}
		String majorVersion = envoyDevice.software.substring(1,2);
		if (majorVersion.matches("\\d")) {
			return Integer.parseInt(majorVersion) > 6;
		}

		return false;
	}

	public String getDefaultPassword() {
		return this.getSerialNumber().substring(this.getSerialNumber().length()-6);
	}

	public String getSoftwareVersion() {
		return envoyDevice.software.isEmpty() ? "Unknown" : envoyDevice.software;
	}

	public String getSerialNumber() {
		return envoyDevice.sn.isEmpty() ? "000000000000" : envoyDevice.sn;
	}

	public String getReleaseDate() {
		return (buildInfo != null) ? Convertors.convertToLocalDateTime(buildInfo.timeGmt).format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
	}

}
