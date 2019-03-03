package com.hz.models.envoy.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class EnvoyDevice {
	@XmlAttribute(name="name")
	public String name;
	@XmlElement
	public String sn;
	@XmlElement
	public String pn;
	@XmlElement
	public String software;
	@XmlElement
	public String euaid;
	@XmlElement
	public String seqnum;
	@XmlElement
	public String apiver;
	@XmlElement
	public String imeter;
}
