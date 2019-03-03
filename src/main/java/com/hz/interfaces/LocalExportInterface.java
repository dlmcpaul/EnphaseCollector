package com.hz.interfaces;

import com.hz.models.database.EnvoySystem;

public interface LocalExportInterface extends ExportServiceInterface {
	public void sendSystemInfo(EnvoySystem envoySystem);
}
