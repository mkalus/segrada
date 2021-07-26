package org.segrada.servlet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.eclipse.jetty.server.Request;
import org.segrada.session.CSRFTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Filter to check for CSRF tokens in POST requests
 */
@Singleton
public class CSRFFilter implements Filter {
	private final static Logger logger = LoggerFactory.getLogger(CSRFFilter.class.getName());

	/**
	 * get injector - not very nice, but works
	 */
	@Inject
	private Injector injector;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("CSRF token filter initialized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest) request;

		// check security token - multipart/form-data has to be checked within the controllers
		if (servletRequest.getMethod().equals("POST") && !servletRequest.getContentType().startsWith("multipart/form-data;")) {
			// get token and session token
			String token = CSRFTokenManager.getTokenFromRequest(servletRequest);
			String sessionToken = CSRFTokenManager.getTokenForSession(servletRequest.getSession());

			if (token == null || !token.equals(sessionToken)) {
				logger.error("CSRF token failed");
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSRF token failed - please reload whole page");
				return; // break filter chain
			}

			// inject request to retrieve it later in SegradaMessageBodyReader
			injector.injectMembers(servletRequest);

			filterChain.doFilter(servletRequest, response);
		} else filterChain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		logger.info("CSRF token filter destroyed");
	}
}
