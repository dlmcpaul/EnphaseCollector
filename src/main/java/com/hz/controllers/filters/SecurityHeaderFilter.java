package com.hz.controllers.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeaderFilter implements Filter {

	@Override
	public void destroy() {
		// No special destroy requirements
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpServletResponse=(HttpServletResponse)response;
		httpServletResponse.setHeader("Content-Security-Policy","default-src 'self' 'unsafe-inline';");
		httpServletResponse.setHeader("X-Frame-Options","SAMEORIGIN");
		httpServletResponse.setHeader("X-Content-Type-Options","nosniff");
		httpServletResponse.setHeader("Referrer-Policy","no-referrer");
		httpServletResponse.setHeader("Feature-Policy","sync-xhr 'self'");
		chain.doFilter(request, response);      // continue execution of other filter chain.
	}

	@Override
	public void init(FilterConfig filterConfig) {
		// No special init requirements
	}

}
