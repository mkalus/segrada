package org.segrada.session;

import org.eclipse.jetty.util.MultiPartInputStreamParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * A manager for the CSRF token for a given session. The {@link #getTokenForSession(HttpSession)} should used to
 * obtain the token value for the current session (and this should be the only way to obtain the token value).
 *
 * @author Eyal Lupu
 * see https://raw.githubusercontent.com/eyal-lupu/eyallupu-blog/master/SpringMVC-3.1-CSRF/src/main/java/com/eyallupu/blog/springmvc/controller/csrf/CSRFTokenManager.java
 */
final public class CSRFTokenManager {
	/**
	 * The token parameter name
	 */
	static final String CSRF_PARAM_NAME = "_csrf";

	/**
	 * The location on the session which stores the token
	 */
	private final static String CSRF_TOKEN_FOR_SESSION_ATTR_NAME = CSRFTokenManager.class.getName() + ".tokenval";

	private CSRFTokenManager() {};

	public static String getTokenForSession(HttpSession session) {
		String token;
		// I cannot allow more than one token on a session - in the case of two requests trying to
		// init the token concurrently
		synchronized (session) {
			token = (String) session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);
			if (null==token) {
				token= UUID.randomUUID().toString();
				session.setAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME, token);
			}
		}
		return token;
	}

	/**
	 * Extracts the token value from the session
	 * @param request
	 * @return
	 */
	public static String getTokenFromRequest(HttpServletRequest request) {
		// JSON POST - will be in HTML header
		if (request.getContentType().startsWith("application/json")) {
			return request.getHeader("X-CSRF-Token");
		}
		return request.getParameter(CSRF_PARAM_NAME);
	}
	
}
