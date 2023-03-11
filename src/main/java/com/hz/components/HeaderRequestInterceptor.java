package com.hz.components;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

	private final String headerName;
	private final String headerValue;

	public HeaderRequestInterceptor(String headerName, String headerValue) {
		this.headerName = headerName;
		this.headerValue = headerValue;
	}

	@Override
	public @NotNull ClientHttpResponse intercept(HttpRequest request, byte @NotNull [] body, ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().add(headerName, headerValue);
		return execution.execute(request, body);
	}
}
