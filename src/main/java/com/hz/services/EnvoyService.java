package com.hz.services;

import com.hz.configuration.EnphaseURLS;
import com.hz.exceptions.ConnectionException;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.json.*;
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

	private long lastReadTime = 0L;
	private boolean readSuccess = false;
	private int fullReadCount = 0;

	private List<Inventory> inventoryList = null;

	public boolean isOk() {
    	return this.readSuccess;
	}

	public LocalDateTime getLastReadTime() {
    	return (lastReadTime > 0L) ? Convertors.convertToLocalDateTime(lastReadTime) : LocalDateTime.now();
	}

	private System getSystemData() throws IOException, URISyntaxException {
		ResponseEntity<System> systemResponse = envoyConnectionProxy.getSecureTemplate().getForEntity(EnphaseURLS.SYSTEM, System.class);

		if (systemResponse.getStatusCode().value() == 200 &&
			systemResponse.getBody() != null) {
				return systemResponse.getBody();
		}
		throw new ConnectionException("Failed to Read " + EnphaseURLS.SYSTEM);
	}

	public Optional<System> collectEnphaseData() {
    	try {
			System system = getSystemData();
		    getProductionData(system);

		    // Wait until production read time is updated
		    long waitTime = 0L;
		    while (systemNotReady(system)) {
			    Thread.sleep(1000);
				waitTime += 1000;
			    getProductionData(system);
	        }
			if (waitTime > 0) {
				log.warn("Waited {} ms", waitTime);
			}

		    Optional<EimType> eim = system.getProduction().getProductionEim();
			this.lastReadTime = eim.map(TypeBase::getReadingTime).orElse(0L);

		    getInventory(system);
		    getIndividualPanelData(system);
		    getDeviceMeters(system);
		    getPowerMeters(system);

		    if (system.getNetwork().isWifi()) {
		    	getWirelessInfo(system);
		    }

		    this.readSuccess = true;
		    return Optional.of(system);
	    } catch (RestClientException | IOException | URISyntaxException e) {
		    log.error("Failed to retrieve Solar stats. Exception was {}", e.getMessage(), e);
	    } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    log.error("Interrupted while reading Solar stats. Exception was {}", e.getMessage(), e);
		}
		this.readSuccess = false;
		return Optional.empty();
	}

	public LocalDateTime getCollectionTime(System system) {
	    Optional<EimType> productionEim = system.getProduction().getProductionEim();
	    // Envoy only produces time in seconds
	    return productionEim.map(typeBase -> Convertors.convertToLocalDateTime(typeBase.getReadingTime()))
			    .orElseGet(() -> LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

	public String getExpiryAsString() {
		return envoyConnectionProxy.getExpiryAsString();
	}

    private boolean systemNotReady(System system) {
	    Optional<EimType> eim = system.getProduction().getProductionEim();
	    return eim.map(typeBase -> typeBase.getReadingTime() <= lastReadTime).orElse(false);
    }

    private void getInventory(System system) throws IOException, URISyntaxException {
	    if (fullReadCount-- <= 0) {
		    fullReadCount = 10;  // Only update every 10 calls.

		    ResponseEntity<List<Inventory>> inventoryResponse =
				    envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.INVENTORY, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });

		    if (inventoryResponse.getStatusCode().value() == 200) {
			    system.setInventoryList(inventoryResponse.getBody());
			    inventoryList = system.getInventoryList();
	        } else {
			    throw new IOException("Reading Inventory failed with status " + inventoryResponse.getStatusCode());
	        }
	    } else {
		    system.setInventoryList(inventoryList);
	    }
    }

	private void getProductionData(System system) throws IOException, URISyntaxException {
		system.setProduction( envoyConnectionProxy.getSecureTemplate().getForObject(EnphaseURLS.PRODUCTION, Production.class) );
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
