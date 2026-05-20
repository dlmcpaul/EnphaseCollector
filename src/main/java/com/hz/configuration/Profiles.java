package com.hz.configuration;

public class Profiles {

	public static final String TESTING = "testing";
	public static final String NOT_TESTING = "!" + TESTING;
	public static final String INFLUXDB = "influxdb";
	public static final String MQTT = "mqtt";
	public static final String PVOUTPUT = "pvoutput";
	public static final String EXPERIMENTAL = "experimental";
	public static final String NOT_EXPERIMENTAL = "!" + EXPERIMENTAL;

	private Profiles() {

	}
}
