package com.hz.controllers.models;

import lombok.Data;

import java.util.Date;

@Data
public class SystemInfo {
	private int production;
	private int consumption;
	private int inverterCount;
	private boolean wifi;
	private Date lastRead;

	public SystemInfo(int production, int consumption, int inverterCount, boolean wifi, Date lastRead) {
		this.production = production;
		this.consumption = consumption;
		this.inverterCount = inverterCount;
		this.wifi = wifi;
		this.lastRead = lastRead;
	}

}
