package org.segrada.servlet;

import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.web.AlreadyCommittedException;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.Header;
import net.sf.ehcache.constructs.web.PageInfo;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
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
 * Customized cache filter that filters out certain pages which should not be cached
 */
public class SegradaSimplePageCachingFilter extends SimplePageCachingFilter {
	private final static Logger logger = LoggerFactory.getLogger(SegradaSimplePageCachingFilter.class.getName());

	/**
	 * excluded patterns
	 */
	private static final Pattern excludePatterns = Pattern.compile("/(add|edit/|search/|locale/)");

	@Override
	protected void doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws AlreadyGzippedException, AlreadyCommittedException, FilterNonReentrantException, LockTimeoutException, Exception {
		// exclude?
		String url = ((Request) servletRequest).getRequestURL().toString();

		if (excludePatterns.matcher(url).find()) {
			// excluded: normal filter chain called
			filterChain.doFilter(servletRequest, servletResponse);

			if (logger.isDebugEnabled())
				logger.debug("Not caching url " + url);
		} else {
			// included cached filter chain loaded
			super.doFilter(servletRequest, servletResponse, filterChain);
		}
	}


	private static final Pattern segradaKeyPatterns = Pattern.compile("^GET.*/(node|source)(.*)$");

	protected String calculateKey(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession();

		// get language from session
		Object lObject = session.getAttribute("language");
		String language = lObject==null?null:(String) lObject;
		if (language == null) language = httpRequest.getLocale().getLanguage();

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(httpRequest.getMethod()).append(language).append(httpRequest.getRequestURI()).append(httpRequest.getQueryString());
		String key = stringBuffer.toString();

		// replace ?page=1
		if (key.endsWith("page=1"))
			key = key.substring(0, key.length()-6).concat("null");

		return key;
	}
}
