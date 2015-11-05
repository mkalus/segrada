package org.segrada.service.repository;

import com.google.inject.Singleton;

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
 * Repository for RememberMe Services
 */
public interface RememberMeRepository {
	/**
	 * create a new token
	 * @param userId to be saved
	 * @return newly created cookie
	 */
	String createTokenForCookie(String userId);

	/**
	 * Remove token from repository
	 * @param token to be removed
	 * @return true if token was removed
	 */
	boolean removeToken(String token);

	/**
	 * validate token (will also delete old tokens)
	 * @param token to be validated
	 * @return userId if token is valid, null otherwise
	 */
	String validateTokenAndGetUserId(String token);
}
