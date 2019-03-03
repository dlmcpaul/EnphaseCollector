package com.hz.models.envoy.xml;

import javax.xml.bind.annotation.XmlElement;

public class EnvoyPackage {
	@XmlElement
	public String pn;
	@XmlElement
	public String version;
	@XmlElement
	public String build;
}
