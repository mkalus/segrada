package org.segrada.model.prototype;

import java.util.Map;

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
 * User Group model interface
 */
public interface IUserGroup extends SegradaEntity {
	//String getTitle();
	void setTitle(String title);

	/**
	 * define user role
	 * @param role to set
	 * @param value to set the role to, 0 = unset/not active, 1 = active, other values may be set in certain circumstances
	 */
	void setRole(String role, int value);

	/**
	 * set role to 1
	 * @param role to set
	 */
	void setRole(String role);

	/**
	 * set role to 0
	 * @param role to unset
	 */
	void unsetRole(String role);

	/**
	 * check whether role is not set 0
	 * @param role to check
	 * @return true, if role not 0
	 */
	boolean hasRole(String role);

	/**
	 * get role value
	 * @param role to check
	 * @return value or 0 if role does not exist
	 */
	int getRole(String role);

	/**
	 * get all roles
	 * @return
	 */
	Map<String, Integer> getRoles();

	/**
	 * @return true, if group is active
	 */
	Boolean getActive();
	void setActive(Boolean active);

	/**
	 * special types of groups have this property set
	 */
	String getSpecial();
	void setSpecial(String special);
}
