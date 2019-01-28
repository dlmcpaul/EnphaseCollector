package com.hz.services;

import com.hz.configuration.EnphaseRestClientConfig;
import com.hz.metrics.Metric;
import com.hz.models.*;
import com.hz.models.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;

/**
 * Created by David on 22-Oct-17.
 */
@Service
public class EnphaseService {

    private static final Logger LOG = LoggerFactory.getLogger(EnphaseService.class);

    private final RestTemplate enphaseRestTemplate;

    private final RestTemplate enphaseSecureRestTemplate;

    @Autowired
	public EnphaseService(RestTemplate enphaseRestTemplate, RestTemplate enphaseSecureRestTemplate) {
		this.enphaseRestTemplate = enphaseRestTemplate;
		this.enphaseSecureRestTemplate = enphaseSecureRestTemplate;
	}

	public Optional<System> collectEnphaseData() {
    	try {
		    ResponseEntity<System> systemResponse = enphaseRestTemplate.getForEntity(EnphaseRestClientConfig.SYSTEM, System.class);

		    if (systemResponse.getStatusCodeValue() == 200) {
			    System system = systemResponse.getBody();
			    getProductionData(system);
			    getInventory(system);
			    getIndividualPanelData(system);
			    return Optional.of(system);
		    } else {
			    LOG.error("Failed to retrieve Solar stats. status was {}", systemResponse.getStatusCodeValue());
		    }
	    } catch (RestClientException e) {
		    LOG.error("Failed to retrieve Solar stats. Exception was {}", e.getMessage());
	    }
		return Optional.empty();
	}

    public Date getCollectionTime(@NotNull System system) {
	    Optional<TypeBase> productionEim = system.getProduction().getProductionEim();
	    if (productionEim.isPresent()) {
//		    return productionEim.get().getReadingTime();
		    LOG.debug("Production read time {}", productionEim.get().getReadingTime());
	    }
	    return GregorianCalendar.getInstance().getTime();
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

	    if (serial.equalsIgnoreCase("121707050571")) {
		    return "1";
	    }
	    if (serial.equalsIgnoreCase("121707050096")) {
		    return "2";
	    }
	    if (serial.equalsIgnoreCase("121707049853")) {
		    return "3";
	    }

	    if (serial.equalsIgnoreCase("121707047544")) {
		    return "4";
	    }
	    if (serial.equalsIgnoreCase("121707049848")) {
		    return "5";
	    }
	    if (serial.equalsIgnoreCase("121707050094")) {
		    return "6";
	    }
	    if (serial.equalsIgnoreCase("121707050367")) {
		    return "7";
	    }
	    if (serial.equalsIgnoreCase("121707040461")) {
		    return "8";
	    }
	    if (serial.equalsIgnoreCase("121707040638")) {
		    return "9";
	    }
	    if (serial.equalsIgnoreCase("121707050013")) {
		    return "10";
	    }

	    if (serial.equalsIgnoreCase("121707049878")) {
		    return "11";
	    }
	    if (serial.equalsIgnoreCase("121707050549")) {
		    return "12";
	    }
	    if (serial.equalsIgnoreCase("121707049876")) {
		    return "13";
	    }
	    if (serial.equalsIgnoreCase("121707050098")) {
		    return "14";
	    }
	    if (serial.equalsIgnoreCase("121707049864")) {
		    return "15";
	    }
	    if (serial.equalsIgnoreCase("121707050570")) {
		    return "16";
	    }

	    LOG.info(serial);

	    return serial;
    }

    private void calculateSavings(ArrayList<Metric> metricList, long production, long consumption) {
	    if (production > consumption) {
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

	    Optional<TypeBase> productionEim = system.getProduction().getProductionEim();
	    long production = 0;
	    long consumption = 0;
	    if (productionEim.isPresent()) {
		    production = productionEim.get().getWattsNow();
		    metricList.add(new Metric("solar.production.current", production));
		    metricList.add(new Metric("solar.production.total", productionEim.get().getWattsLifetime()));
	    }

	    Optional<TypeBase> consumptionEim = system.getProduction().getConsumptionEim();
	    if (consumptionEim.isPresent()) {
		    consumption = consumptionEim.get().getWattsNow();
		    metricList.add(new Metric("solar.consumption.current", consumption));
		    metricList.add(new Metric("solar.consumption.total", consumptionEim.get().getWattsLifetime()));
	    }

	    calculateSavings(metricList, production, consumption);

	    system.getProduction().getInverterList().forEach(inverter -> metricList.add(new Metric("solar.panel-" + map(inverter.getSerialNumber()), inverter.getLastReportWatts())));

	    return metricList;
    }

    private void getInventory(@NotNull System system) {
	    ResponseEntity<List<Inventory>> inventoryResponse =
			    enphaseRestTemplate.exchange(EnphaseRestClientConfig.INVENTORY, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });

	    if (inventoryResponse.getStatusCodeValue() == 200) {
		    system.setInventoryList(inventoryResponse.getBody());
	    } else {
		    LOG.error("Reading Inventory failed {}", inventoryResponse.getStatusCode());
	    }
    }

	private void getProductionData(@NotNull System system) {
		system.setProduction( enphaseRestTemplate.getForObject(EnphaseRestClientConfig.PRODUCTION, Production.class) );
	}

	private void getIndividualPanelData(@NotNull System system) {
	    // Individual Panel values
	    ResponseEntity<List<Inverter>> inverterResponse =
			    enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.INVERTERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inverter>>() { });

	    if (inverterResponse.getStatusCodeValue() == 200) {
		    system.getProduction().setInverterList(inverterResponse.getBody());
	    } else {
	    	LOG.error("Reading Invertors failed {}", inverterResponse.getStatusCode());
	    }
    }

}
