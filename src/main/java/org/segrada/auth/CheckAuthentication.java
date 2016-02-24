package org.segrada.auth;

import com.google.inject.Injector;
import com.google.inject.servlet.SessionScoped;
import com.sun.jersey.api.view.Viewable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * Method interceptor to test authentication
 */
@SessionScoped
public class CheckAuthentication implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(CheckAuthentication.class);

    /**
     * reference to injector
     */
    private static Injector injector;

    /**
     * set the injector - called by Bootstrap
     */
    public static void setInjector(Injector injector)
    {
        CheckAuthentication.injector = injector;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // get method
        Method method = invocation.getMethod();

        // get identity
        Identity identity = injector.getInstance(Identity.class);

        //Access allowed for all
        if (!method.isAnnotationPresent(PermitAll.class)) {
            //Access denied for all
            if (method.isAnnotationPresent(DenyAll.class))
                return returnAccessDenied();

            //Verify user access
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                //TODO: check multiple roles
                //if (!rolesSet.contains(identity.getRole()))
                //    return returnAccessDenied();
            }
        } else if (identity == null) return returnAccessDenied(); // permit all will still check the existence of identity

        if (logger.isDebugEnabled())
            logger.debug("Authentication passed.");

        return invocation.proceed();
    }

    /**
     * helper to return access denied
     * @return null
     * @throws Exception on IO errors
     */
    private static Object returnAccessDenied() throws Exception {
        logger.info("ACCESS DENIED!");

        HttpServletResponse response = injector.getInstance(HttpServletResponse.class);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);

        return null;
    }
}
