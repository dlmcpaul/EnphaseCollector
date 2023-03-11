package com.hz.components;

import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class EnphaseRequestRetryStrategy extends DefaultHttpRequestRetryStrategy {

	@Override
	public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
		int maxRetries = 3;
		return (execCount < maxRetries) && (exception instanceof UnknownHostException || exception instanceof HttpHostConnectException || exception instanceof SocketTimeoutException);
	}

	@Override
	public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
		return TimeValue.of(15, TimeUnit.SECONDS);
	}

}
