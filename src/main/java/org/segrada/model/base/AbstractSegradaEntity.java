package org.segrada.model.base;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;

import javax.annotation.Nullable;

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
 * Base model for all entities
 */
abstract public class AbstractSegradaEntity implements SegradaEntity {
	/**
	 * document version (may be 0)
	 */
	private int version = 0;

	/**
	 * document id (may be null)
	 */
	private String id = null;

	private Long created = 0L;

	private Long modified = 0L;

	private IUser creator;

	private IUser modifier;

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public Long getCreated() {
		return created;
	}
	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getModified() {
		return modified;
	}
	public void setModified(Long modified) {
		this.modified = modified;
	}

	public @Nullable IUser getCreator() {
		return creator;
	}
	public void setCreator(IUser creator) {
		this.creator = creator;
	}

	public @Nullable IUser getModifier() {
		return modifier;
	}
	public void setModifier(IUser modifier) {
		this.modifier = modifier;
	}

	@Override
	public String getModelName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "created", "modified", "creator", "modifier");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "created", "modified", "creator", "modifier");
	}

	@Override
	public String toString() {
		return "{" + getModelName() + "}" + (getId() == null ? "*" : getId()) + ", " + getTitle();
	}
}
