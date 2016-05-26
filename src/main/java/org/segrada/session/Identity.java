package org.segrada.session;

import com.google.inject.servlet.SessionScoped;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.IUserGroup;
import org.segrada.model.prototype.SegradaEntity;

import java.io.Serializable;
import java.util.Map;

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
	private static final long serialVersionUID = 1L;

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

	public boolean hasRole(String role) {
		return isAuthenticated() && this.user.getGroup().hasRole(role);
	}

	/**
	 * checks whether any role fits
	 * @param roles comma separated list of possible roles
	 * @return true if any role matches
	 */
	public boolean hasAnyRole(String roles) {
		String[] role = roles.split(",");

		for(int i=0; i<role.length; i++){
			if (hasRole(role[i])) return true;
		}
		return false;
	}

	/**
	 * check access, also include admin check
	 * @param roles comma separated list of possible roles
	 * @return true if any role matches or is admin
	 */
	public boolean hasAccess(String roles) {
		return isAuthenticated() && (hasRole("ADMIN") || hasAnyRole(roles));
	}

	/**
	 * return true if edit access for this entity
	 * @param entity to check
	 * @param type of entity, e.g. NODE
	 * @return true if edit access available
	 */
	public boolean hasEditAccess(SegradaEntity entity, String type) {
		return hasXAccess(entity, type + "_EDIT");
	}

	/**
	 * return true if delete access for this entity
	 * @param entity to check
	 * @param type of entity, e.g. NODE
	 * @return true if delete access available
	 */
	public boolean hasDeleteAccess(SegradaEntity entity, String type) {
		return hasXAccess(entity, type + "_DELETE");
	}

	/**
	 * helper for hasEditAccess and hasDeleteAccess
	 * @param entity to check
	 * @param action of entity, e.g. NODE
	 * @return true if access granted
	 */
	private boolean hasXAccess(SegradaEntity entity, String action) {
		if (entity == null || !isAuthenticated()) return false;

		// special user group ADMIN may not be edited or deleted
		if (entity.getModelName().equals("UserGroup") &&
				"ADMIN".equals(((IUserGroup) entity).getSpecial())) return false;

		if (hasRole("ADMIN") || hasRole(action)) return true;

		if (hasRole(action + "_MINE")) {
			IUser creator = entity.getCreator();

			return creator != null && creator.getId().matches(getId());
		}
		return false;
	}

	public int getRole(String role) {
		return this.user==null?0:this.user.getGroup().getRole(role);
	}

	public Map<String, Integer> getRoles() {
		return this.user==null?null:this.user.getGroup().getRoles();
	}

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
