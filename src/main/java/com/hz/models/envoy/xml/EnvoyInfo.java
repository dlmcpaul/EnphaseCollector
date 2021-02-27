package com.hz.models.envoy.xml;

import com.hz.utils.Convertors;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
	@XmlElement(name="build_info", required = false, nillable = true)
	public BuildInfo buildInfo;

	// Only needed for test bean
	public EnvoyInfo(String softwareVersion, String serialNumber) {
		this.envoyDevice = new EnvoyDevice();
		this.envoyDevice.software = softwareVersion;
		this.envoyDevice.sn = serialNumber;
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
