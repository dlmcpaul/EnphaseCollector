package com.hz.components;

import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A retry strategy that retries 3 times with a 15-second wait time between.
 * The IOExceptions selected are based on observed intermittent errors returned from Enphase
 */
public class EnphaseRequestRetryStrategy implements HttpRequestRetryStrategy {

	private final Set<Class<? extends IOException>> retryIOExceptionClasses;

	public EnphaseRequestRetryStrategy() {
		retryIOExceptionClasses = new HashSet<>(
				Arrays.asList(
						UnknownHostException.class,
						HttpHostConnectException.class,
						SocketTimeoutException.class
				)
		);
	}

	@Override
	public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
		return (execCount < 3) && (this.retryIOExceptionClasses.contains(exception.getClass()));
	}

	@Override
	public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
		return false;
	}

	@Override
	public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
		return TimeValue.of(15, TimeUnit.SECONDS);
	}

}
