package org.segrada.config;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.RequestScoped;
import org.segrada.auth.CheckAuthentication;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

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
 * Method to include authentication
 */
public class AuthenticationModule extends AbstractModule {
    @Override
    protected void configure() {
        CheckAuthentication checkAuthentication = getCheckAuthentication();

        // bind auth checker to methods annotated with respective security annotations
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(PermitAll.class),
                checkAuthentication);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(DenyAll.class),
                checkAuthentication);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(RolesAllowed.class),
                checkAuthentication);
    }

    @Provides
    @RequestScoped
    @Inject
    public CheckAuthentication getCheckAuthentication() {
        return new CheckAuthentication();
    }
}
