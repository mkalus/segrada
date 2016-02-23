package org.segrada.servlet;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.sf.ehcache.constructs.web.PageInfo;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;
import org.apache.commons.codec.binary.Hex;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.security.MessageDigest;
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
@Singleton
public class SegradaSimplePageCachingFilter extends SimplePageCachingFilter {
	private final static Logger logger = LoggerFactory.getLogger(SegradaSimplePageCachingFilter.class.getName());

	/**
	 * excluded patterns
	 */
	private static final Pattern excludePatterns = Pattern.compile("/(clear_cache|reindex|locale/)");

	/**
	 * url parts that add a session key to the cache key in order to function properly
	 */
	private static final Pattern addSessionToCacheKey = Pattern.compile("^/((node|source|file|relation)(/by_tag/[0-9\\-]+)?|relation_type|pictogram|tag|color)$");

	/**
	 * pattern to filter out jsessionid-urls
	 */
	private static final Pattern jSessionFilter = Pattern.compile(";jsessionid=[a-zA-Z0-9]+$");

	/**
	 * reference to injector
	 */
	private static Injector injector;

	/**
	 * set the injector - called by Bootstrap
	 */
	public static void setInjector(Injector injector)
	{
		SegradaSimplePageCachingFilter.injector = injector;
	}

	@Override
	protected void doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws Exception {
		// exclude?
		String url = servletRequest.getRequestURL().toString();

		// session set?
		Identity identity = injector.getInstance(Identity.class);
		boolean loggedIn = identity.getName()!=null;

		// only get request and matched urls when logged in and not on POST methods
		if (!loggedIn || servletRequest.getMethod().equals("POST") || excludePatterns.matcher(url).find()) {
			if (logger.isDebugEnabled())
				logger.debug("Not caching url " + url);

			// build page info and return gzipped if needed
			PageInfo pageInfo = this.buildPage(servletRequest, servletResponse, filterChain);
			if(pageInfo.isOk()) {
				if(!servletResponse.isCommitted()) {
					//throw new AlreadyCommittedException("Response already committed after doing buildPage but before writing response from PageInfo.");
					this.writeResponse(servletRequest, servletResponse, pageInfo);
				}
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

		// filter out jsessionid URL addition, just in case - should not happen, because it is not clean, but nevertheless
		urlPart = jSessionFilter.matcher(urlPart).replaceFirst(""); //TODO: might be faster to use lastIndexOf and substr to replace this instead of regex

		// query data map - later to be sorted into a key
		SortedMap<String, String> queryData = new TreeMap<>();

		// match with urls that require session key addition to cache in order to function properly?
		MatchResult matchResult = addSessionToCacheKey.matcher(urlPart).toMatchResult();
		if (((Matcher) matchResult).find()) {
			// this is the same as the session key
			String controller = matchResult.group().substring(1, 2).toUpperCase() + matchResult.group().substring(2) + "Service";
			// get session data
			Object controllerData = session.getAttribute(controller);

			// create sorted map
			if (controllerData != null && controllerData instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> d = (Map<String, Object>)controllerData;
				for (Map.Entry<String, Object> stringObjectEntry : d.entrySet()) {
					Object o = stringObjectEntry.getValue();
					String value;
					// concatenate string array in order to make caching work
					if (o instanceof String[]) value = String.join(",", (String[]) o);
					else // all other cases: convert to string
						value = stringObjectEntry.getValue().toString();
					queryData.put(stringObjectEntry.getKey(), value);
				}
			}
		}

		// get query data and add it to list (overwrite session, if needed)
		boolean clearTags = false; // flag to check whether parameter clearTags was sent in form data
		boolean tagsSetByForm =  false; // flag to check whether there has been a field tags in form data
		for (Map.Entry<String, String[]> parameter : httpRequest.getParameterMap().entrySet()) {
			String key = parameter.getKey();
			if (key.equals("clearTags")) {
				clearTags = true;
				continue; // do not add to parameters
			}
			if (key.equals("tags")) tagsSetByForm = true; // tags were in form data
			queryData.put(key, String.join(",", parameter.getValue()));
		}
		// did we have field clearTags in form, but no tags were set? => delete tags saved in session, if needed
		if (clearTags && ! tagsSetByForm) queryData.remove("tags"); // will be removed from session via controller

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

		// convert query string to md5
		String qs = queryString.toString();
		// do we actually need to convert to md5? qs are not so long in general
		if (!qs.isEmpty())
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				qs = Hex.encodeHexString(md.digest(qs.getBytes("UTF-8")));
			} catch (Exception e) {
				// do nothing - just use query string as it is
			}

		// get session id
		//Identity identity = injector.getInstance(Identity.class);
		//identity.getId()
		//TODO: add this later after we have added ACLs - not all users see the same stuff

		// create key
		return httpRequest.getMethod() + language + urlPart + qs;
	}
}
