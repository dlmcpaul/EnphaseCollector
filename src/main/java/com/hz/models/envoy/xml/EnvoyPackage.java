package com.hz.models.envoy.xml;


import jakarta.xml.bind.annotation.XmlElement;

public class EnvoyPackage {

	public EnvoyPackage(String pn) {
		this.pn = pn;
	}

	@XmlElement
	public String pn;
	@XmlElement
	public String version;
	@XmlElement
	public String build;
}
