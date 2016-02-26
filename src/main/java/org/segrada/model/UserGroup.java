package org.segrada.model;

import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.IUserGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
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
 * User Group model implementation
 */
public class UserGroup extends AbstractSegradaEntity implements IUserGroup {
	private static final long serialVersionUID = 1L;

	/**
	 * Group title
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String title = "";

	/**
	 * special type of group?
	 */
	private String special;

	/**
	 * keeps roles
	 */
	private Map<String, Integer> roles;

	/**
	 * Constructor
	 */
	public UserGroup() {
		setCreated(System.currentTimeMillis());
		setModified(System.currentTimeMillis());
		roles = new HashMap<>();
	}

	@Override
	public void setRole(String role, int value) {
		if (value == 0) roles.remove(role);
		else roles.put(role, value);
	}

	@Override
	public void setRole(String role) {
		setRole(role, 1);
	}

	@Override
	public void unsetRole(String role) {
		roles.remove(role);
	}

	@Override
	public boolean hasRole(String role) {
		return roles.containsKey(role);
	}

	@Override
	public int getRole(String role) {
		Integer value = roles.get(role);
		return value==null?0:value;
	}

	@Override
	public Map<String, Integer> getRoles() {
		return roles;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getSpecial() {
		return special;
	}

	@Override
	public void setSpecial(String special) {
		this.special = special;
	}
}
