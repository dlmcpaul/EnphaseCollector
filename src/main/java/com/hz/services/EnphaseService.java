package com.hz.services;

import com.hz.configuration.EnphaseRestClientConfig;
import com.hz.metrics.Metric;
import com.hz.models.*;
import com.hz.models.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by David on 22-Oct-17.
 */
public class EnphaseService {

    private static final Logger LOG = LoggerFactory.getLogger(EnphaseService.class);

    private final RestTemplate enphaseRestTemplate;

    private final RestTemplate enphaseSecureRestTemplate;

	private final RestTemplate destinationRestTemplate;

	public EnphaseService(RestTemplate enphaseRestTemplate, RestTemplate enphaseSecureRestTemplate, RestTemplate destinationRestTemplate) {
		this.enphaseRestTemplate = enphaseRestTemplate;
		this.enphaseSecureRestTemplate = enphaseSecureRestTemplate;
		this.destinationRestTemplate = destinationRestTemplate;
	}

	@Scheduled(fixedRate = 60000)
	public void gather() {
		try {
			this.uploadMetrics(this.collect());
		} catch (IOException e) {
			LOG.error("Metric Collection failed error was : {}", e.getMessage(), e);
		}
	}

    public System collect() throws IOException {
	    // Global System values
	    System system = null;
        ResponseEntity<System> systemResponse = enphaseRestTemplate.getForEntity(EnphaseRestClientConfig.SYSTEM, System.class);

        if (systemResponse.getStatusCodeValue() == 200) {
            system = systemResponse.getBody();

	        // Inventory of system
            ResponseEntity<List<Inventory>> inventoryResponse =
                    enphaseRestTemplate.exchange(EnphaseRestClientConfig.INVENTORY, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() { });

            system.setInventoryList(inventoryResponse.getBody());

	        Production production = enphaseRestTemplate.getForObject(EnphaseRestClientConfig.PRODUCTION, Production.class);
	        system.setProduction(production);

	        // Individual Panel values
            ResponseEntity<List<Inverter>> inverterResponse =
                    enphaseSecureRestTemplate.exchange(EnphaseRestClientConfig.INVERTERS, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inverter>>() { });

            system.getProduction().setInverterList(inverterResponse.getBody());
        } else {
	        LOG.error("Failed to retrieve Solar stats. status was {}", systemResponse.getStatusCodeValue());
        }

        return system;
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

	    return serial;
    }

    private void calculateSavings(ArrayList<Metric> metricList, final long time, long production, long consumption) {
	    if (production > consumption) {
		    metricList.add(new Metric(time,"solar.excess", production, consumption));
		    metricList.add(new Metric(time,"solar.savings", consumption));
	    } else {
		    metricList.add(new Metric(time,"solar.excess", 0));
		    metricList.add(new Metric(time,"solar.savings", production));
	    }
	    metricList.add(new Metric(time, "solar.difference",production, consumption));
    }

    private List<Metric> getMetrics(System system) {
	    ArrayList<Metric> metricList = new ArrayList<>();

	    Optional<TypeBase> productionEim = system.getProduction().getProductionEim();
	    final long time;
	    long production = 0;
	    long consumption = 0;
	    if (productionEim.isPresent()) {
		    time = productionEim.get().getReadingTime().getTime();
		    production = productionEim.get().getWattsNow();
		    metricList.add(new Metric(time, "solar.production.current", production));
		    metricList.add(new Metric(time, "solar.production.total", productionEim.get().getWattsLifetime()));
	    } else {
		    time = 0;
	    }

	    Optional<TypeBase> consumptionEim = system.getProduction().getConsumptionEim();
	    if (consumptionEim.isPresent()) {
		    consumption = consumptionEim.get().getWattsNow();
		    metricList.add(new Metric(time, "solar.consumption.current",consumption));
		    metricList.add(new Metric(time, "solar.consumption.total", consumptionEim.get().getWattsLifetime()));
	    }

	    calculateSavings(metricList, time, production, consumption);

	    system.getProduction().getInverterList().forEach(inverter -> metricList.add(new Metric(time, "solar.panel." + map(inverter.getSerialNumber()), inverter.getLastReportWatts())));

	    return metricList;
    }

    public void uploadMetrics(System system) {
	    List<Metric> metrics = getMetrics(system);

	    //curl -i -XPOST 'http://localhost:8086/write?db=mydb' --data-binary 'cpu_load_short,host=server01,region=us-west value=0.64 1434055562000000000'
	    String request = metrics.stream()
			    .map(Metric::dataPoint)
			    .collect(Collectors.joining("\n"));
	    try {
		    ResponseEntity<String> response = destinationRestTemplate.postForEntity("/write?db=solardb&precision=s", request, String.class);
		    if (response.getStatusCodeValue() != 204) {
			    LOG.error("Failed to Post data Point : {}", response.getBody());
		    }
		} catch (HttpClientErrorException e) {
		    LOG.error(request);
			LOG.error("uploadMetrics failed {}", e.getMessage(), e);
		}
    }

}
