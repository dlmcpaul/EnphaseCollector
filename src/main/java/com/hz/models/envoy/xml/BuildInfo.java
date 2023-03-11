package com.hz.models.envoy.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class BuildInfo {
	@XmlElement(name="build_id")
	public String id;
	@XmlElement(name="build_time_gmt")
	public long timeGmt;
	@XmlElement(name="release_ver")
	public String releaseVersion;
	@XmlElement(name="release_stage")
	public String releaseStage;
}
