package org.segrada.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.IUser;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
 * User model implementation
 */
public class User extends AbstractSegradaEntity implements IUser {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "error.notNull")
	@Size(min=4, max=16, message = "error.login.size")
	private String login;

	@NotNull(message = "error.notNull")
	@Size(min=5, max=25, message = "error.password.size")
	private String password;

	transient private String confirmPassword;

	@NotNull(message = "error.notNull")
	@Size(min=2, max=64, message = "error.name.size")
	private String name;

	@NotNull(message = "error.notNull")
	@Pattern(regexp="^(ADMIN|USER)$", message = "error.role.type")
	private String role;

	private Long created;

	private Long modified;

	private Long lastLogin;

	private Boolean active;

	/**
	 * Constructor
	 */
	public User() {
		created = modified = System.currentTimeMillis();
		active = true;
	}

	@Override
	public String getLogin() {
		return login;
	}

	@Override
	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getRole() {
		return role;
	}

	@Override
	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String getTitle() {
		return getName();
	}

	@Override
	public Long getCreated() {
		return created;
	}

	@Override
	public void setCreated(Long created) {
		this.created = created;
	}

	@Override
	public Long getModified() {
		return modified;
	}

	@Override
	public void setModified(Long modified) {
		this.modified = modified;
	}

	@Override
	public Long getLastLogin() {
		return lastLogin;
	}

	@Override
	public void setLastLogin(Long lastLogin) {
		this.lastLogin = lastLogin;
	}

	@Override
	public Boolean getActive() {
		return active;
	}

	@Override
	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	@AssertTrue(message="{user.password.match}")
	public boolean passwordsMatch() {
		return this.password == null && this.confirmPassword == null ||
				this.password != null && this.password.equals(this.confirmPassword);
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "created", "modified", "lastLogin");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "created", "modified", "lastLogin");
	}

	@Override
	public String toString() {
		return "{User}" + (getId() == null ? "*" : getId()) + ", " + getName() + ", " + getLogin() + (!getActive()?" [inactive]":"");
	}
}
