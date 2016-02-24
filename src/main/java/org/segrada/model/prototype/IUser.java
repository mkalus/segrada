package org.segrada.model.prototype;

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
 * User model interface
 */
public interface IUser extends SegradaEntity {
	/**
	 * @return login of user
	 */
	String getLogin();
	void setLogin(String login);

	/**
	 * @return password of user
	 */
	String getPassword();
	void setPassword(String password);

	/**
	 * @return full name of user
	 */
	String getName();
	void setName(String name);

	IUserGroup getGroup();
	void setGroup(IUserGroup group);

	/**
	 * @return last login date
	 */
	Long getLastLogin();
	void setLastLogin(Long lastLogin);

	/**
	 * @return true, if user is active
	 */
	Boolean getActive();
	void setActive(Boolean active);
}