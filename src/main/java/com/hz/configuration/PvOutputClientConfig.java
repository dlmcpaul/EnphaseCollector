package com.hz.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
@Profile("pvoutput")
public class PvOutputClientConfig {
	private static final Logger LOG = LoggerFactory.getLogger(PvOutputClientConfig.class);

	private EnphaseCollectorProperties config;

	@Autowired
	public PvOutputClientConfig(EnphaseCollectorProperties config) {
		this.config = config;
	}

	public static String ADD_STATUS = "/service/r2/addstatus.jsp";

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

		LOG.info("Writing to Pv Output endpoint {} using key {}", config.getPvOutputResource().getUrl(), config.getPvOutputResource().getKey());

		RestTemplate template = builder
				.rootUri(config.getPvOutputResource().getUrl())
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(30))
				.additionalInterceptors(new HeaderRequestInterceptor("X-Pvoutput-Apikey",config.getPvOutputResource().getKey()))
				.additionalInterceptors(new HeaderRequestInterceptor("X-Pvoutput-SystemId",config.getPvOutputResource().getSystemId()))
				.build();

		return template;
	}

	public static class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

		private final String headerName;
		private final String headerValue;

		HeaderRequestInterceptor(String headerName, String headerValue) {
			this.headerName = headerName;
			this.headerValue = headerValue;
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
			request.getHeaders().set(headerName, headerValue);
			return execution.execute(request, body);
		}
	}
}
