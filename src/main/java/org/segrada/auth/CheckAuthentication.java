package org.segrada.auth;

import com.google.inject.Injector;
import com.google.inject.servlet.SessionScoped;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
 * Method interceptor to test authentication (injected via AOP)
 *
 * For a list of Roles see org.segrada.controller.UserGroupController
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

            // is admin?
            if (!identity.hasRole("ADMIN")) {
                //Verify user access
                if (method.isAnnotationPresent(RolesAllowed.class)) {
                    RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                    Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                    // check multiple roles
                    boolean matched = false;
	                boolean isAllEdit = false;
	                boolean isAllDelete = false;
	                boolean isOwnEdit = false;
	                boolean isOwnDelete = false;
	                boolean isAdd = false;
                    for (String role : rolesSet)
                        if (identity.getRoles().containsKey(role)) {
                            matched = true;
	                        if (role.endsWith("_EDIT_MINE")) isOwnEdit = true;
	                        else if (role.endsWith("_EDIT")) isAllEdit = true;
	                        else if (role.endsWith("_DELETE_MINE")) isOwnDelete = true;
	                        else if (role.endsWith("_DELETE")) isAllDelete = true;
	                        else if (role.endsWith("_ADD")) isAdd = true;
                        }

                    if (!matched)
                        return returnAccessDenied();

	                // method had add/own edit or own delete set
	                if (isAdd || isOwnEdit || isOwnDelete) {
		                // new entity? Must have add right!
		                if (isNewSegradaEntity(invocation)) {
			                if (!isAdd) return returnAccessDenied();
			                // add right will pass here
		                } else if ((isOwnEdit && !isAllEdit) || (isOwnDelete && !isAllDelete)) {
			                // check if we have to check own edit/delete, but not all edit/delete -> check entity!

			                // get entity
			                SegradaEntity entity = getSegradaEntityFromFirstArgument(invocation);
			                // try to get creator
			                IUser creator = entity!=null?entity.getCreator():null;

			                // does creator id match session id?
			                if (creator == null || !creator.getId().matches(identity.getId()))
				                return returnAccessDenied();
			                // all other cases pass
		                }
	                }
                }
            }
        } else if (identity == null) return returnAccessDenied(); // permit all will still check the existence of identity

        if (logger.isDebugEnabled())
            logger.debug("Authentication passed.");

        return invocation.proceed();
    }

	/**
	 * get segrada entity from method invoction's first agrument
	 * @param invocation method invocation
	 * @return entity or null, if nothing found
	 */
	private static SegradaEntity getSegradaEntityFromFirstArgument(MethodInvocation invocation) {
		// sanity checks
		if (invocation.getArguments() == null || invocation.getArguments().length == 0) return null;

		// get first argument
		Object argument = invocation.getArguments()[0];

		// check type of argument
		if (argument instanceof SegradaEntity) // SegradaEntity -> easy
			return (SegradaEntity) argument;
		if (argument instanceof String) { // String, might be id or uid
			String id = (String) argument;
			if (id.isEmpty()) return null; // empty string = empty entity

			// get class instance
			AbstractBaseController controller = (AbstractBaseController) invocation.getThis();

			// is this an uid?
			String realId = controller.getService().convertUidToId(id);
			if (realId != null) id = realId; // replace!

			return controller.getService().findById(id);
		}

		// all other cases => null entity
		return null;
	}

	/**
	 * check if entity in first argument is new
	 * @param invocation method invocation
	 * @return true on new entity
	 */
	private static boolean isNewSegradaEntity(MethodInvocation invocation) {
		// no arguments -> this is new entity, because this happens only in add methods
		if (invocation.getArguments() == null || invocation.getArguments().length == 0) return true;

		// get first argument
		Object argument = invocation.getArguments()[0];

		// check type of argument
		if (argument instanceof SegradaEntity) {
			SegradaEntity entity = (SegradaEntity) argument;
			return entity.getId() == null || entity.getId().isEmpty(); // true on empty id
		}
		if (argument instanceof String)
			return ((String)argument).isEmpty(); // true on empty string
		if (argument == null) return true;

		// in all other cases
		return false;
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
