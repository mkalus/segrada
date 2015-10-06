package org.segrada.servlet;

import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.web.AlreadyCommittedException;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.PageInfo;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.MatchResult;
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
	private static final Pattern excludePatterns = Pattern.compile("/(clear_cache|reindex|locale/)");

	/**
	 * url parts that add a session key to the cache key in order to function properly
	 */
	private static final Pattern addSessionToCacheKey = Pattern.compile("^/(node|source|file|relation|relation_type|pictogram|tag|color)$");

	@Override
	protected void doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws AlreadyGzippedException, AlreadyCommittedException, FilterNonReentrantException, LockTimeoutException, Exception {
		// exclude?
		String url = servletRequest.getRequestURL().toString();

		// only get request and matched urls
		if (servletRequest.getMethod().equals("POST") || excludePatterns.matcher(url).find()) {
			if (logger.isDebugEnabled())
				logger.debug("Not caching url " + url);

			// build page info and return gzipped if needed
			PageInfo pageInfo = this.buildPage(servletRequest, servletResponse, filterChain);
			if(pageInfo.isOk()) {
				if(servletResponse.isCommitted()) {
					throw new AlreadyCommittedException("Response already committed after doing buildPage but before writing response from PageInfo.");
				}

				this.writeResponse(servletRequest, servletResponse, pageInfo);
			}
		} else {
			// included cached filter chain loaded
			super.doFilter(servletRequest, servletResponse, filterChain);
		}
	}

	/**
	 * calculate key for page from httpRequest
	 * @param httpRequest the request
	 * @return cache key
	 */
	protected String calculateKey(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession();

		// get language from session
		Object lObject = session.getAttribute("language");
		String language = lObject==null?null:(String) lObject;
		if (language == null) language = httpRequest.getLocale().getLanguage();

		// get url, context path stripped
		String urlPart = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

		// query data map - later to be sorted into a key
		SortedMap<String, String> queryData = new TreeMap<>();

		// match with urls that require session key addition to cache in order to function properly?
		MatchResult matchResult = addSessionToCacheKey.matcher(urlPart).toMatchResult();
		if (((Matcher) matchResult).find()) {
			String controller = matchResult.group().substring(1, 2).toUpperCase() + matchResult.group().substring(2) + "Service";
			// get session data
			Object controllerData = session.getAttribute(controller);

			// create sorted map
			if (controllerData != null && controllerData instanceof Map) {
				Map<String, String> d = (Map<String, String>)controllerData;
				for (String parameter : d.keySet()) {
					queryData.put(parameter, d.get(parameter));
				}
			}
		}

		// get query data and add it to list (overwrite session, if needed)
		Enumeration<String> parameters = httpRequest.getParameterNames();
		while (parameters.hasMoreElements()) {
			String parameter = parameters.nextElement();
			String value = httpRequest.getParameter(parameter);

			queryData.put(parameter, value);
		}

		// create query string as key
		StringBuilder queryString = new StringBuilder();
		for (Map.Entry<String, String> entry: queryData.entrySet()) {
			try {
				String encodedName = URLEncoder.encode(entry.getKey(), "UTF-8");
				Object value = entry.getValue();
				String encodedValue = value!=null?URLEncoder.encode(String.valueOf(value), "UTF-8"):"";
				// do not include page=1
				if (encodedName.equals("page") && encodedValue.equals("1")) continue;
				if (queryString.length() > 0)
					queryString.append('&');
				queryString.append(encodedName).append("=").append(encodedValue);
			} catch (Exception e) {
				logger.warn("Could not encode field " + entry.getKey() + " with value " + entry.getValue(), e);
			}
		}

		// create key
		return httpRequest.getMethod() + language + urlPart + queryString.toString();
	}
}
