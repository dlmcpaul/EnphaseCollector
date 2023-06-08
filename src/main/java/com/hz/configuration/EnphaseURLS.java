package com.hz.configuration;

public class EnphaseURLS {

	public static final String SYSTEM = "/home.json";   // Basic info about the envoy device
	public static final String INVENTORY = "/inventory.json?deleted=1";
	public static final String PRODUCTION = "/production.json?details=1";   // Used for production and consumption values
	public static final String CONTROLLER = "/info.xml";    // Used to generate passwords and detect enphase software version

	// Needs Digest authentication for < V7
	public static final String INVERTERS = "/api/v1/production/inverters";      // Used for individual panel reads
	public static final String WIFI_INFO = "/admin/lib/wireless_display.json?site_info=0";  // Wireless data
	public static final String WAN_INFO = "/admin/lib/network_display.json";
	public static final String DEVICE_METERS = "/ivp/meters";
	public static final String POWER_METERS = "/ivp/meters/readings";
	public static final String LIVE_STATUS = "/ivp/livedata/status";
	public static final String DEVICE_STATUS = "/ivp/sc/status";

	// Needs Installer authentication for < V7
	public static final String METER_STREAM = "/stream/meter";

	// Needed for V7 to set the cookie
	public static final String AUTH_CHECK = "/auth/check_jwt";

	private EnphaseURLS() {}
}
