package org.segrada.session;

import com.google.inject.servlet.SessionScoped;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.IUserGroup;

import java.io.Serializable;

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
 * Keeps the current user session
 */
@SessionScoped
public class Identity implements Serializable {
	static final long serialVersionUID = 1L;

	/**
	 * reference to user
	 */
	protected IUser user;

	public IUser getUser() {
		return user;
	}

	public void setUser(IUser user) {
		this.user = user;
	}

	public String getId() {
		return this.user==null?null:this.user.getId();
	}

	public String getName() {
		return this.user==null?null:this.user.getName();
	}

	public IUserGroup getUserGroup() {
		return this.user==null?null:this.user.getGroup();
	}

	//TODO

	/**
	 * @return true if user is authenticated/logged in
	 */
	public boolean isAuthenticated() {
		return getId() != null;
	}

	/**
	 * log out user
	 */
	public void logout() {
		this.user = null;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "name");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "name");
	}

	@Override
	public String toString() {
		IUserGroup group = getUserGroup();
		return "{Identity}" + (isAuthenticated()?(getId() + "," + (group!=null?group.getTitle() + ",":"") + getName()):("*notLoggedIn*"));
	}
}
