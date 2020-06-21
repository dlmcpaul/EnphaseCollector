package com.hz.configuration;

import com.hz.components.HeaderRequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Log4j2
@Profile("pvoutput")
public class PvOutputClientConfig {
	private final EnphaseCollectorProperties config;

	public static final String ADD_STATUS = "/service/r2/addstatus.jsp";
	public static final String GET_STATUS = "/service/r2/getstatus.jsp";

	// HTTP Post to https://pvoutput.org/service/r2/addstatus.jsp
	// add headers
	// X-Pvoutput-Apikey = key
	// X-Pvoutput-SystemId = systemid
	// body is name=value pairs from below
	// name desc                mandatory   format      type        example
	// d	Date	            Yes	        yyyymmdd	date	    20100830
	// t	Time	            Yes	        hh:mm	    time	    14:12
	// v1	Energy Generation	No	        number	    watt hours	10000
	// v2	Power Generation	No	        number	    watts	    2000
	// v3	Energy Consumption	No	        number	    watt hours	10000
	// v4	Power Consumption	No	        number	    watts	    2000
	// v5	Temperature	        No	        decimal	    celsius	    23.4
	// v6	Voltage	            No	        decimal	    volts	    210.7
	// c1	Cumulative Flag	    No	        number	    -	        1
	// n	Net Flag	        No	        number	    -	        1


	@Bean(name="pvRestTemplate")
	public RestTemplate pvRestTemplate(RestTemplateBuilder builder) {

		log.info("Writing to Pv Output endpoint {} using system id {}", config.getPvOutputResource().getUrl(), config.getPvOutputResource().getSystemId());

		return builder
				.rootUri(config.getPvOutputResource().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.additionalInterceptors(new HeaderRequestInterceptor("X-Pvoutput-Apikey", config.getPvOutputResource().getKey()))
				.additionalInterceptors(new HeaderRequestInterceptor("X-Pvoutput-SystemId", config.getPvOutputResource().getSystemId()))
				.build();
	}

}
