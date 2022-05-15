package com.hz.services;

import com.hz.configuration.EnphaseRestClientConfig;
import com.hz.configuration.EnphaseSecureRestClientConfig;
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
import org.springframework.web.client.RestTemplate;

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
public class EnphaseService {

	private long lastReadTime = 0L;
	private int lastStatus = 200;
	private int fullReadCount = 0;

	private List<Inventory> inventoryList = null;

    private final RestTemplate enphaseRestTemplate;
    private final RestTemplate enphaseSecureRestTemplate;

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
    	return this.lastStatus == 200;
	}

	public LocalDateTime getLastReadTime() {
    	return (lastReadTime > 0L) ? Convertors.convertToLocalDateTime(lastReadTime) : LocalDateTime.now();
	}

	public Optional<System> collectEnphaseData() {
    	try {
		    ResponseEntity<System> systemResponse = enphaseRestTemplate.getForEntity(EnphaseRestClientConfig.SYSTEM, System.class);
			this.lastStatus = systemResponse.getStatusCodeValue();

		    if (systemResponse.getStatusCodeValue() == 200) {
			    System system = systemResponse.getBody();
			    if (system != null) {
				    getProductionData(system);
				    // Wait until production read time is updated
				    while (systemNotReady(system)) {
					    getProductionData(system);
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

				    return Optional.of(system);
			    } else {
				    log.error("Envoy Production read failed");
			    }
		    } else {
			    log.error("Failed to retrieve Solar stats. status was {}", systemResponse.getStatusCodeValue());
		    }
	    } catch (RestClientException e) {
		    log.error("Failed to retrieve Solar stats. Exception was {}", e.getMessage(), e);
	    }
		return Optional.empty();
	}

	public LocalDateTime getCollectionTime(System system) {
	    Optional<EimType> productionEim = system.getProduction().getProductionEim();
	    // Envoy only produces time in seconds
	    return productionEim.map(typeBase -> Convertors.convertToLocalDateTime(typeBase.getReadingTime()))
			    .orElseGet(() -> LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    private boolean systemNotReady(System system) {
	    Optional<EimType> eim = system.getProduction().getProductionEim();

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

    private void getInventory(System system) {
	    if (fullReadCount-- <= 0) {
		    fullReadCount = 10;  // Only update every 10 calls.

		    ResponseEntity<List<Inventory>> inventoryResponse =
				    enphaseRestTemplate.exchange(EnphaseRestClientConfig.INVENTORY, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });
	        this.lastStatus = inventoryResponse.getStatusCodeValue();

		    if (inventoryResponse.getStatusCodeValue() == 200) {
			    system.setInventoryList(inventoryResponse.getBody());
			    inventoryList = system.getInventoryList();
	        } else {
		        log.error("Reading Inventory failed {}", inventoryResponse.getStatusCode());
	        }
	    } else {
		    system.setInventoryList(inventoryList);
	    }
    }

	private void getProductionData(System system) {
		system.setProduction( enphaseRestTemplate.getForObject(EnphaseRestClientConfig.PRODUCTION, Production.class) );
	}

	private void getDeviceMeters(System system) {
    	try {

		    HttpHeaders headers = new HttpHeaders();
		    headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
		    HttpEntity<String> entity = new HttpEntity<>(headers);

		    ResponseEntity<List<DeviceMeter>> deviceMeterResponse =
				    enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.DEVICE_METERS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<DeviceMeter>>() {
				    });
		    this.lastStatus = deviceMeterResponse.getStatusCodeValue();

		    if (deviceMeterResponse.getStatusCodeValue() == 200) {
			    system.getProduction().setDeviceMeterList(deviceMeterResponse.getBody());
		    } else {
			    log.error("Reading Device Meters failed {}", deviceMeterResponse.getStatusCode());
		    }
	    } catch (RestClientException e) {
    		log.warn("Reading Device Meters failed {}", e.getMessage());
		    system.getProduction().setDeviceMeterList(new ArrayList<>());
	    }

	}

	private void getPowerMeters(System system) {
    	try {
		    HttpHeaders headers = new HttpHeaders();
		    headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
		    HttpEntity<String> entity = new HttpEntity<>(headers);

			ResponseEntity<List<PowerMeter>> powerMeterResponse =
					enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.POWER_METERS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<PowerMeter>>() { });
			this.lastStatus = powerMeterResponse.getStatusCodeValue();

			if (powerMeterResponse.getStatusCodeValue() == 200) {
				system.getProduction().setPowerMeterList(powerMeterResponse.getBody());
			} else {
				log.error("Reading Power Meters failed {}", powerMeterResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			log.warn("Reading Power Meters failed {}", e.getMessage());
		    system.getProduction().setPowerMeterList(new ArrayList<>());
		}
	}

	private void getIndividualPanelData(System system) {
	    // Individual Panel values
	    ResponseEntity<List<Inverter>> inverterResponse =
			    enphaseSecureRestTemplate.exchange(EnphaseSecureRestClientConfig.INVERTERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inverter>>() { });
		this.lastStatus = inverterResponse.getStatusCodeValue();

	    if (inverterResponse.getStatusCodeValue() == 200) {
		    system.getProduction().setInverterList(inverterResponse.getBody());
	    } else {
	    	log.error("Reading Inverters failed {}", inverterResponse.getStatusCode());
	    }
    }

    private void getWirelessInfo(System system) {
		ResponseEntity<Wireless> wirelessResponse =
				enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.WIFI_INFO, HttpMethod.GET, null, new ParameterizedTypeReference<Wireless>() { });
		this.lastStatus = wirelessResponse.getStatusCodeValue();

		if (wirelessResponse.getStatusCodeValue() == 200) {
			system.setWireless(wirelessResponse.getBody());
		} else {
			log.error("Reading Wireless failed {}", wirelessResponse.getStatusCode());
		}
	}

}
