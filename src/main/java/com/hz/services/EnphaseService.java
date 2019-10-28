package com.hz.services;

import com.hz.configuration.EnphaseRestClientConfig;
import com.hz.metrics.Metric;
import com.hz.models.envoy.json.System;
import com.hz.models.envoy.json.*;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.utils.Convertors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.xml.transform.StringSource;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by David on 22-Oct-17.
 */
@Service
public class EnphaseService {

    private static final Logger LOG = LoggerFactory.getLogger(EnphaseService.class);

	private long lastReadTime = 0L;
	private int lastStatus = 200;
	private int fullReadCount = 0;

	private EnvoyInfo envoyInfo = null;
	private List<Inventory> inventoryList = null;

	private final Unmarshaller enphaseMarshaller;
    private final RestTemplate enphaseRestTemplate;
    private final RestTemplate enphaseSecureRestTemplate;

    // Table of my serial numbers to map to simpler values
	private List<String> mySerialNumbers = Arrays.asList(
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

    @Autowired
	public EnphaseService(RestTemplate enphaseRestTemplate, RestTemplate enphaseSecureRestTemplate, Unmarshaller enphaseMarshaller) {
		this.enphaseRestTemplate = enphaseRestTemplate;
		this.enphaseSecureRestTemplate = enphaseSecureRestTemplate;
		this.enphaseMarshaller = enphaseMarshaller;
    }

	public boolean isOk() {
    	return this.lastStatus == 200;
	}

	public LocalDateTime getLastReadTime() {
    	if (lastReadTime > 0L) {
    		return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastReadTime * 1000L), ZoneId.systemDefault());
	    }

    	return LocalDateTime.now();
	}

	public String getSoftwareVersion() {
    	this.getControllerData();
    	if (envoyInfo != null) {
    		return envoyInfo.envoyDevice.software;
	    }

    	return "Unknown";
	}

	public String getSerialNumber() {
		this.getControllerData();
		if (envoyInfo != null) {
			return envoyInfo.envoyDevice.sn;
		}

		return "Unknown";
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
				    LOG.error("Envoy Production read failed");
			    }
		    } else {
			    LOG.error("Failed to retrieve Solar stats. status was {}", systemResponse.getStatusCodeValue());
		    }
	    } catch (RestClientException e) {
		    LOG.error("Failed to retrieve Solar stats. Exception was {}", e.getMessage());
	    }
		return Optional.empty();
	}

	public LocalDateTime getCollectionTime(@NotNull System system) {
	    Optional<EimType> productionEim = system.getProduction().getProductionEim();
	    // Envoy only produces time in seconds
	    return productionEim.map(typeBase -> LocalDateTime.ofInstant(Instant.ofEpochMilli(typeBase.getReadingTime() * 1000L), ZoneId.systemDefault()))
			    .orElseGet(() -> LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    private boolean systemNotReady(@NotNull System system) {
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
		    metricList.add(new Metric("solar.excess", production, consumption));
		    metricList.add(new Metric("solar.savings", consumption));
	    } else {
		    metricList.add(new Metric("solar.excess", 0));
		    metricList.add(new Metric("solar.savings", production));
	    }
	    metricList.add(new Metric( "solar.difference",production, consumption));
    }

	public List<Metric> getMetrics(System system) {
	    ArrayList<Metric> metricList = new ArrayList<>();

	    Optional<EimType> productionEim = system.getProduction().getProductionEim();
		Optional<InvertersType> inverter = system.getProduction().getInverter();
	    BigDecimal production = BigDecimal.ZERO;
		BigDecimal consumption = BigDecimal.ZERO;
	    if (productionEim.isPresent() && inverter.isPresent()) {
		    production = system.getProduction().getProductionWatts();
	    	LOG.debug("production eim time {} eim {} inverter time {} inverter {} {}", Convertors.convertToLocalDateTime(productionEim.get().getReadingTime()), productionEim.get().getWattsNow(), Convertors.convertToLocalDateTime(inverter.get().getReadingTime()), production, inverter.get().getWattsNow());
		    metricList.add(new Metric("solar.production.current", production, 5));
		    metricList.add(new Metric("solar.production.total", inverter.get().getWattsLifetime()));
		    metricList.add(new Metric("solar.production.voltage", productionEim.get().getRmsVoltage().floatValue()));
	    }

	    Optional<EimType> consumptionEim = system.getProduction().getTotalConsumptionEim();
	    if (consumptionEim.isPresent()) {
		    consumption = system.getProduction().getConsumptionWatts();
		    LOG.debug("consumption {} {}", Convertors.convertToLocalDateTime(consumptionEim.get().getReadingTime()), consumption);
		    metricList.add(new Metric("solar.consumption.current", consumption));
		    metricList.add(new Metric("solar.consumption.total", consumptionEim.get().getWattsLifetime()));
	    }

	    calculateSavings(metricList, production, consumption);

	    system.getProduction().getMicroInvertorsList().forEach(micro -> metricList.add(new Metric("solar.panel-" + map(micro.getSerialNumber()), micro.getLastReportWatts(), 5)));

	    return metricList;
    }

    private void getInventory(@NotNull System system) {
	    if (fullReadCount-- <= 0) {
		    fullReadCount = 10;  // Only update every 10 calls.

		    ResponseEntity<List<Inventory>> inventoryResponse =
				    enphaseRestTemplate.exchange(EnphaseRestClientConfig.INVENTORY, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });
	        this.lastStatus = inventoryResponse.getStatusCodeValue();

		    if (inventoryResponse.getStatusCodeValue() == 200) {
			    system.setInventoryList(inventoryResponse.getBody());
			    inventoryList = system.getInventoryList();
	        } else {
		        LOG.error("Reading Inventory failed {}", inventoryResponse.getStatusCode());
	        }
	    } else {
		    system.setInventoryList(inventoryList);
	    }
    }

	private void getProductionData(@NotNull System system) {
		system.setProduction( enphaseRestTemplate.getForObject(EnphaseRestClientConfig.PRODUCTION, Production.class) );
	}

	private void getDeviceMeters(@NotNull System system) {
    	try {
		    ResponseEntity<List<DeviceMeter>> deviceMeterResponse =
				    enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.DEVICE_METERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<DeviceMeter>>() {
				    });
		    this.lastStatus = deviceMeterResponse.getStatusCodeValue();

		    if (deviceMeterResponse.getStatusCodeValue() == 200) {
			    system.getProduction().setDeviceMeterList(deviceMeterResponse.getBody());
		    } else {
			    LOG.error("Reading Device Meters failed {}", deviceMeterResponse.getStatusCode());
		    }
	    } catch (RestClientException e) {
    		LOG.warn("Reading Device Meters failed {}", e.getMessage());
		    system.getProduction().setDeviceMeterList(new ArrayList<>());
	    }

	}

	private void getPowerMeters(@NotNull System system) {
    	try {
			ResponseEntity<List<PowerMeter>> powerMeterResponse =
					enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.POWER_METERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<PowerMeter>>() { });
			this.lastStatus = powerMeterResponse.getStatusCodeValue();

			if (powerMeterResponse.getStatusCodeValue() == 200) {
				system.getProduction().setPowerMeterList(powerMeterResponse.getBody());
			} else {
				LOG.error("Reading Power Meters failed {}", powerMeterResponse.getStatusCode());
			}
		} catch (RestClientException e) {
			LOG.warn("Reading Power Meters failed {}", e.getMessage());
		    system.getProduction().setPowerMeterList(new ArrayList<>());
		}
	}

	private void getIndividualPanelData(@NotNull System system) {
	    // Individual Panel values
	    ResponseEntity<List<Inverter>> inverterResponse =
			    enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.INVERTERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inverter>>() { });
		this.lastStatus = inverterResponse.getStatusCodeValue();

	    if (inverterResponse.getStatusCodeValue() == 200) {
		    system.getProduction().setInverterList(inverterResponse.getBody());
	    } else {
	    	LOG.error("Reading Inverters failed {}", inverterResponse.getStatusCode());
	    }
    }

    private void getWirelessInfo(System system) {
		ResponseEntity<Wireless> wirelessResponse =
				enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.WIFI_INFO, HttpMethod.GET, null, new ParameterizedTypeReference<Wireless>() { });
		this.lastStatus = wirelessResponse.getStatusCodeValue();

		if (wirelessResponse.getStatusCodeValue() == 200) {
			system.setWireless(wirelessResponse.getBody());
		} else {
			LOG.error("Reading Wireless failed {}", wirelessResponse.getStatusCode());
		}
	}

	private void getControllerData() {
    	if (null == envoyInfo) {
		    String infoXml = enphaseRestTemplate.getForObject(EnphaseRestClientConfig.CONTROLLER, String.class);

		    try {
		    	if (infoXml != null) {
				    envoyInfo = (EnvoyInfo) enphaseMarshaller.unmarshal(new StringSource(infoXml));
			    }
		    } catch (IOException e) {
		    	LOG.warn("Failed to read envoy info page.  Exception was {}", e.getMessage());
		    }
	    }

    }

}
