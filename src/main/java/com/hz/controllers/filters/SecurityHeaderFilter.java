package com.hz.controllers.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeaderFilter implements Filter {

	private final String contentSecurityPolicyValue;

	public SecurityHeaderFilter(@Qualifier(value="contentSecurityPolicyValue") String contentSecurityPolicyValue) {
		this.contentSecurityPolicyValue = contentSecurityPolicyValue;
	}

	@Override
	public void destroy() {
		// No special destroy requirements
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpServletResponse=(HttpServletResponse)response;
		httpServletResponse.setHeader("Content-Security-Policy", contentSecurityPolicyValue);
		httpServletResponse.setHeader("X-Frame-Options","SAMEORIGIN");
		httpServletResponse.setHeader("X-Content-Type-Options","nosniff");
		httpServletResponse.setHeader("Referrer-Policy","no-referrer");
		httpServletResponse.setHeader("Permissions-Policy","sync-xhr=(self)");
		chain.doFilter(request, response);      // continue execution of other filter chain.
	}

	@Override
	public void init(FilterConfig filterConfig) {
		// No special init requirements
	}

}
