package com.hz.models.envoy.xml;

import javax.xml.bind.annotation.XmlElement;

public class BuildInfo {
	@XmlElement(name="build_id")
	public String id;
	@XmlElement(name="build_time_gmt")
	public long timeGmt;
}
