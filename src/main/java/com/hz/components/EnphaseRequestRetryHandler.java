package com.hz.components;

import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class EnphaseRequestRetryHandler extends StandardHttpRequestRetryHandler {

	public EnphaseRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
		super(retryCount, requestSentRetryEnabled);
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

		if (executionCount <= this.getRetryCount()) {
			// These exceptions appear in the logs occasionally, the enphase device probably needs some more time
			if (exception instanceof UnknownHostException || exception instanceof HttpHostConnectException || exception instanceof SocketTimeoutException) {
				try {
					TimeUnit.SECONDS.sleep(15);
				} catch (InterruptedException e) {
				}
				return true;
			}
		}
		return super.retryRequest(exception, executionCount, context);
	}
}
