package com.hz.services;

import com.hz.configuration.EnphaseURLS;
import com.hz.metrics.Metric;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
	private System cachedSystem;

	private List<Inventory> inventoryList = null;

    // Table of my serial numbers to map to simpler values
	private final List<String> mySerialNumbers = Arrays.asList(
		    "121707050571",
		    "121707050096",
		    "121707049853",
		    "121707047544",
		    "121707049848",
		    "121707050094",
		    "121707050367",
		    "121707040461",
		    "121707040638",
		    "121707050013",
		    "121707049878",
		    "121707050549",
		    "121707049876",
		    "121707050098",
		    "121707049864",
		    "121707050570");

	public boolean isOk() {
    	return this.readSuccess;
	}

	public LocalDateTime getLastReadTime() {
    	return (lastReadTime > 0L) ? Convertors.convertToLocalDateTime(lastReadTime) : LocalDateTime.now();
	}

	private System getSystemData() {
		if (cachedSystem == null) {
			ResponseEntity<System> systemResponse = envoyConnectionProxy.getDefaultTemplate().getForEntity(EnphaseURLS.SYSTEM, System.class);

			if (systemResponse.getStatusCodeValue() == 200) {
				if (systemResponse.getBody() != null) {
					log.info("Fetched Base System Data");
					cachedSystem = systemResponse.getBody();
					return cachedSystem;
				}
			}

			throw new RuntimeException("Failed to Read " + EnphaseURLS.SYSTEM);
		}
		return cachedSystem;
	}

	public Optional<System> collectEnphaseData() {
    	try {
		    log.info("Starting Envoy Data Collection");
			System system = getSystemData();
		    getProductionData(system);

		    // Wait until production read time is updated
		    log.info("Waiting for production data to be ready");
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
			if (eim.isPresent()) {
				this.lastReadTime = eim.map(TypeBase::getReadingTime).orElse(0L);
			}

			log.info("Reading Inventory with read time {}", this.lastReadTime);
		    getInventory(system);
		    log.info("Reading Individual Panels");
		    getIndividualPanelData(system);
		    log.info("Reading Device Meters");
		    getDeviceMeters(system);
		    log.info("Reading Power Meters");
		    getPowerMeters(system);

		    if (system.getNetwork().isWifi()) {
		    	getWirelessInfo(system);
		    }

		    log.info("Envoy Data Collection Successful");
		    this.readSuccess = true;
		    return Optional.of(system);
	    } catch (RestClientException | IOException e) {
		    log.error("Failed to retrieve Solar stats. Exception was {}", e.getMessage(), e);
	    } catch (InterruptedException e) {
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
		if (eim.isEmpty()) {
			return true;
		}
		log.info("System Read time {}", eim.map(typeBase -> typeBase.getReadingTime()));
	    return eim.map(typeBase -> typeBase.getReadingTime() <= lastReadTime).orElse(true);
    }

    private String map(String serial) {
	    //  X X
	    //  X X
	    //    11
	    //    12
	    //  1 2 3
	    //
	    //         4
	    //         5
	    //         6
	    //         7
		//
	    //         8
	    //         9
	    //         10

	    if (mySerialNumbers.contains(serial)) {
	    	return String.valueOf(mySerialNumbers.indexOf(serial) + 1);
	    }

	    return serial;
    }

    private void calculateSavings(ArrayList<Metric> metricList, BigDecimal production, BigDecimal consumption) {
	    if (production.compareTo(consumption) > 0) {
		    metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, production, consumption));
		    metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, consumption));
			metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, 0));
	    } else {
		    metricList.add(new Metric(Metric.METRIC_SOLAR_EXCESS, 0));
		    metricList.add(new Metric(Metric.METRIC_SOLAR_SAVINGS, production));
		    metricList.add(new Metric(Metric.METRIC_GRID_IMPORT, consumption, production));
	    }
	    metricList.add(new Metric( Metric.METRIC_SOLAR_DIFFERENCE, production, consumption));
    }

	public List<Metric> getMetrics(System system) {
	    ArrayList<Metric> metricList = new ArrayList<>();

	    BigDecimal production = system.getProduction().getProductionWatts();
		BigDecimal consumption = system.getProduction().getConsumptionWatts();

		metricList.add(new Metric(Metric.METRIC_PRODUCTION_CURRENT, production, 5));
		metricList.add(new Metric(Metric.METRIC_CONSUMPTION_CURRENT, consumption));
		metricList.add(new Metric(Metric.METRIC_PRODUCTION_VOLTAGE, system.getProduction().getProductionVoltage().floatValue()));

		Optional<EimType> productionEim = system.getProduction().getProductionEim();
		Optional<InvertersType> inverter = system.getProduction().getInverter();
		if (productionEim.isPresent() && inverter.isPresent()) {
	    	log.debug("production: eim time {} eim {} inverter time {} inverter {} calculated {}", Convertors.convertToLocalDateTime(productionEim.get().getReadingTime()), productionEim.get().getWattsNow(), Convertors.convertToLocalDateTime(inverter.get().getReadingTime()), inverter.get().getWattsNow(), production);
		    metricList.add(new Metric(Metric.METRIC_PRODUCTION_TOTAL, inverter.get().getWattsLifetime()));
	    }

	    Optional<EimType> consumptionEim = system.getProduction().getTotalConsumptionEim();
	    if (consumptionEim.isPresent()) {
		    log.debug("consumption: eim time {} eim {} calculated {}", Convertors.convertToLocalDateTime(consumptionEim.get().getReadingTime()), consumptionEim.get().getWattsNow(), consumption);
		    metricList.add(new Metric(Metric.METRIC_CONSUMPTION_TOTAL, consumptionEim.get().getWattsLifetime()));
	    }

	    calculateSavings(metricList, production, consumption);

	    system.getProduction().getMicroInvertersList().forEach(micro -> metricList.add(Metric.createPanelMetric(map(micro.getSerialNumber()), micro.getLastReportWatts(), 5)));

	    return metricList;
    }

    private void getInventory(System system) throws IOException {
	    if (fullReadCount-- <= 0) {
		    fullReadCount = 10;  // Only update every 10 calls.

		    ResponseEntity<List<Inventory>> inventoryResponse =
				    envoyConnectionProxy.getDefaultTemplate().exchange(EnphaseURLS.INVENTORY, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });

		    if (inventoryResponse.getStatusCodeValue() == 200) {
			    system.setInventoryList(inventoryResponse.getBody());
			    inventoryList = system.getInventoryList();
	        } else {
			    throw new IOException("Reading Inventory failed with status " + inventoryResponse.getStatusCode());
	        }
	    } else {
		    system.setInventoryList(inventoryList);
	    }
    }

	private void getProductionData(System system) {
		system.setProduction( envoyConnectionProxy.getDefaultTemplate().getForObject(EnphaseURLS.PRODUCTION, Production.class) );
	}

	private void getDeviceMeters(System system) throws IOException {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
	    HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<List<DeviceMeter>> deviceMeterResponse =
					envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.DEVICE_METERS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<DeviceMeter>>() {
					});

			if (deviceMeterResponse.getStatusCodeValue() == 200) {
				system.getProduction().setDeviceMeterList(deviceMeterResponse.getBody());
			} else {
				throw new IOException("Reading Device Meters failed with status " + deviceMeterResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			log.warn("Software version does not support {}", EnphaseURLS.DEVICE_METERS);
			system.getProduction().setDeviceMeterList(new ArrayList<>());
		}
	}

	private void getPowerMeters(System system) throws IOException {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
	    HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<List<PowerMeter>> powerMeterResponse =
					envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.POWER_METERS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<PowerMeter>>() { });

			if (powerMeterResponse.getStatusCodeValue() == 200) {
				system.getProduction().setPowerMeterList(powerMeterResponse.getBody());
			} else {
				throw new IOException("Reading Power Meters failed with status " + powerMeterResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			log.warn("Software version does not support {}", EnphaseURLS.POWER_METERS);
			system.getProduction().setPowerMeterList(new ArrayList<>());
		}
	}

	private void getIndividualPanelData(System system) throws IOException {
	    // Individual Panel values
	    ResponseEntity<List<Inverter>> inverterResponse =
			    envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.INVERTERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inverter>>() { });

	    if (inverterResponse.getStatusCodeValue() == 200) {
		    system.getProduction().setInverterList(inverterResponse.getBody());
	    } else {
		    throw new IOException("Reading Inverters failed with status " + inverterResponse.getStatusCode());
	    }
    }

    private void getWirelessInfo(System system) throws IOException {
		ResponseEntity<Wireless> wirelessResponse =
				envoyConnectionProxy.getSecureTemplate().exchange(EnphaseURLS.WIFI_INFO, HttpMethod.GET, null, new ParameterizedTypeReference<Wireless>() { });

		if (wirelessResponse.getStatusCodeValue() == 200) {
			system.setWireless(wirelessResponse.getBody());
		} else {
			throw new IOException("Reading Wireless Info failed with status " + wirelessResponse.getStatusCode());
		}
	}

}
