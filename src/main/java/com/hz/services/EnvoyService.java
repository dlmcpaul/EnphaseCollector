package com.hz.services;

import com.hz.configuration.EnphaseURLS;
import com.hz.exceptions.ConnectionException;
import com.hz.models.envoy.interfaces.Power;
import com.hz.models.envoy.json.*;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.utils.Convertors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by David on 22-Oct-17.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class EnvoyService {
	private final EnvoyConnectionProxy envoyConnectionProxy;
	private final EnvoyInfo envoyInfo;

	private LocalDateTime lastProductionCollectionTime = null;
	private LocalDateTime lastSystemReadTime = null;
	private boolean readSuccess = true;
	private System system = null;

	public boolean isOk() {
    	return this.readSuccess;
	}

	public LocalDateTime getLastProductionCollectionTime() {
    	return (lastProductionCollectionTime != null) ? lastProductionCollectionTime : LocalDateTime.now();
	}

	private LocalDateTime getLastSystemReadTime() {
		return (lastSystemReadTime != null) ? lastSystemReadTime : LocalDateTime.now();
	}

	private System getSystemData() throws IOException, URISyntaxException {
		if (system != null && getLastSystemReadTime().isAfter(LocalDateTime.now().minusMinutes(15))) {
			return system;
		}

		lastSystemReadTime = LocalDateTime.now();

		ResponseEntity<System> systemResponse = envoyConnectionProxy.getSecureTemplate().getForEntity(EnphaseURLS.SYSTEM, System.class);

		if (systemResponse.getStatusCode().value() == 200 &&
			systemResponse.getBody() != null) {
				return systemResponse.getBody();
		}
		throw new ConnectionException("Failed to Read " + EnphaseURLS.SYSTEM);
	}

	public Optional<System> collectEnphaseData(boolean supportBattery) {
    	try {
			system = getSystemData();   // Basic Data (cached)
		    // Consumption & Production
		    // Consumption & Production (cacheable?)
		    if (envoyInfo.isV7orAbove()) {
				getProductionDataV7(system);
		    } else {
				getProductionDataV5(system);
		    }
		    getDeviceMeters(system);    // Consumption & Production (cacheable?)
		    getPowerMeters(system);     // Consumption & Production

		    getInventory(system);       // Has some Battery Info

		    if (supportBattery) {
			    getBatteryData(system);     // Battery Charging
		    }
		    getIndividualPanelData(system); // Panel Level Data

		    if (system.getNetwork().isWifi()) {
		    	getWirelessInfo(system);    // WIFI
		    }

		    this.lastProductionCollectionTime = getCollectionTime(system);
		    this.readSuccess = true;
		    return Optional.of(system);
	    } catch (RestClientException | IOException | URISyntaxException e) {
		    log.error("Failed to retrieve Solar stats. Exception was {}", e.getMessage(), e);
		}
		this.readSuccess = false;
		return Optional.empty();
	}

	public LocalDateTime getCollectionTime(System system) {
	    Optional<Power> productionEim = system.getProduction().getProductionMeter();
	    // Envoy only produces time in seconds
	    return productionEim.map(typeBase -> Convertors.convertToLocalDateTime(typeBase.getTimestamp()))
			    .orElseGet(() -> LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

	public String getExpiryAsString() {
		return envoyConnectionProxy.getExpiryAsString();
	}

	private void getBatteryData(System system) throws IOException, URISyntaxException {

		ResponseEntity<BatteryPower> batteriesResponse =
				envoyConnectionProxy.getSecureTemplate().getForEntity(EnphaseURLS.BATTERY_POWER, BatteryPower.class);

		if (batteriesResponse.getStatusCode().value() == 200) {
			assert batteriesResponse.getBody() != null;
			system.setBatteries(batteriesResponse.getBody().getDevices());
		} else {
			throw new IOException("Reading Battery Data failed with status " + batteriesResponse.getStatusCode());
		}
	}

    private void getInventory(System system) throws IOException, URISyntaxException {

	 //   ResponseEntity<String> inventoryStr = envoyConnectionProxy.getSecureTemplate().getForEntity(EnphaseURLS.INVENTORY, String.class);
	 //   ResponseEntity<String> deviceListStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/device_list", String.class);
	 //   ResponseEntity<String> statusStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/status", String.class);
	 //   ResponseEntity<String> secctrlStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/secctrl", String.class);
	 //   ResponseEntity<String> devicestatusStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/device_status", String.class);
	 //   ResponseEntity<String> statusStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/livedata/status", String.class);
	 //   ResponseEntity<String> statusStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/status", String.class);
	 //   ResponseEntity<String> statusStr = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/device_status", String.class);
	 //   ResponseEntity<String> statusStr2 = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/device_list", String.class);
	 //   ResponseEntity<String> statusStr3 = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/secctrl", String.class);
	 //   ResponseEntity<String> statusStr4 = envoyConnectionProxy.getSecureTemplate().getForEntity("/ivp/ensemble/status", String.class);

	    String url = envoyInfo.isV7orAbove() ? EnphaseURLS.INVENTORY : EnphaseURLS.INVENTORY_V5;

	    ResponseEntity<List<Inventory>> inventoryResponse =
			    envoyConnectionProxy.getSecureTemplate()
					    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });

	    if (inventoryResponse.getStatusCode().value() == 200) {
		    system.setInventoryList(inventoryResponse.getBody());
        } else {
		    throw new IOException("Reading Inventory failed with status " + inventoryResponse.getStatusCode());
        }
    }

	private void getProductionDataV5(System system) throws IOException, URISyntaxException {
		system.setProduction(envoyConnectionProxy.getSecureTemplate().getForObject(EnphaseURLS.PRODUCTION, Production.class));
	}

	private void getProductionDataV7(System system) throws IOException, URISyntaxException {
		system.setProduction(new Production());

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<List<MeterReport>> meterReportResponse =
					envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.DEVICE_METERS + "/reports", HttpMethod.GET, entity, new ParameterizedTypeReference<List<MeterReport>>() {
					});

			if (meterReportResponse.getStatusCode().value() == 200) {
				system.getProduction().setMeterReportList(meterReportResponse.getBody());
			} else {
				throw new IOException("Reading Device Meters failed with status " + meterReportResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			log.warn("Device does not support {}", EnphaseURLS.DEVICE_METERS + "/reports");
			system.getProduction().setMeterReportList(new ArrayList<>());
		}

	}

	private void getDeviceMeters(System system) throws IOException, URISyntaxException {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
	    HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<List<DeviceMeter>> deviceMeterResponse =
					envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.DEVICE_METERS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<DeviceMeter>>() {
					});

			if (deviceMeterResponse.getStatusCode().value() == 200) {
				system.getProduction().setDeviceMeterList(deviceMeterResponse.getBody());
			} else {
				throw new IOException("Reading Device Meters failed with status " + deviceMeterResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			log.warn("Device does not support {}", EnphaseURLS.DEVICE_METERS);
			system.getProduction().setDeviceMeterList(new ArrayList<>());
		}
	}

	private void getPowerMeters(System system) throws IOException, URISyntaxException {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
	    HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<List<PowerMeter>> powerMeterResponse =
					envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.POWER_METERS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<PowerMeter>>() { });

			if (powerMeterResponse.getStatusCode().value() == 200) {
				system.getProduction().setPowerMeterList(powerMeterResponse.getBody());
			} else {
				throw new IOException("Reading Power Meters failed with status " + powerMeterResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			log.warn("Device does not support {}", EnphaseURLS.POWER_METERS);
			system.getProduction().setPowerMeterList(new ArrayList<>());
		}
	}

	private void getIndividualPanelData(System system) throws IOException, URISyntaxException {
	    // Individual Panel values
	    ResponseEntity<List<Inverter>> inverterResponse =
			    envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.INVERTERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inverter>>() { });

	    if (inverterResponse.getStatusCode().value() == 200) {
		    system.getProduction().setInverterList(inverterResponse.getBody());
	    } else {
		    throw new IOException("Reading Inverters failed with status " + inverterResponse.getStatusCode());
	    }
    }

    private void getWirelessInfo(System system) throws IOException, URISyntaxException {
		ResponseEntity<Wireless> wirelessResponse =
				envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.WIFI_INFO, HttpMethod.GET, null, new ParameterizedTypeReference<Wireless>() { });

		if (wirelessResponse.getStatusCode().value() == 200) {
			system.setWireless(wirelessResponse.getBody());
		} else {
			throw new IOException("Reading Wireless Info failed with status " + wirelessResponse.getStatusCode());
		}
	}

}
