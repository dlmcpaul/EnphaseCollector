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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created by David on 22-Oct-17.
 */
@Service
public class EnphaseService {

    private static final Logger LOG = LoggerFactory.getLogger(EnphaseService.class);

    private final RestTemplate enphaseRestTemplate;

    private final RestTemplate enphaseSecureRestTemplate;

    private static long lastReadTime = 0L;

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
	public EnphaseService(RestTemplate enphaseRestTemplate, RestTemplate enphaseSecureRestTemplate) {
		this.enphaseRestTemplate = enphaseRestTemplate;
		this.enphaseSecureRestTemplate = enphaseSecureRestTemplate;
	}

	private static void setLastReadTime(long time) {
    	EnphaseService.lastReadTime = time;
	}

	public Optional<System> collectEnphaseData() {
    	try {
		    ResponseEntity<System> systemResponse = enphaseRestTemplate.getForEntity(EnphaseRestClientConfig.SYSTEM, System.class);

		    if (systemResponse.getStatusCodeValue() == 200) {
			    System system = systemResponse.getBody();
			    getProductionData(system);
			    // Wait until production read time is updated
			    while (systemNotReady(system)) {
				    getProductionData(system);
			    }

			    Optional<TypeBase> eim = system.getProduction().getProductionEim();
			    setLastReadTime(eim.isPresent() ? eim.get().getReadingTime() : 0L);

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
		    Calendar lastRead = GregorianCalendar.getInstance();
		    lastRead.setTimeInMillis(productionEim.get().getReadingTime() * 1000L);
		    return lastRead.getTime();
	    }
	    return GregorianCalendar.getInstance().getTime();
    }

    private boolean systemNotReady(@NotNull System system) {
	    Optional<TypeBase> eim = system.getProduction().getProductionEim();

    	if (eim.isPresent()) {
    		return eim.get().getReadingTime() <= lastReadTime;
	    }

    	return true;
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
