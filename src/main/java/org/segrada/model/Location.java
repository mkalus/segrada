package org.segrada.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Range;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.ILocation;

import javax.validation.constraints.NotNull;

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
 * Location model implementation
 */
public class Location extends AbstractSegradaEntity implements ILocation {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "{error.notNull}")
	private String parentId;

	@NotNull(message = "{error.notNull}")
	private String parentModel;

	@NotNull(message = "{error.notNull}")
	@Range(min = -90, max = 90, message = "{error.rangeLatCoordinate}")
	private Double latitude;

	@NotNull(message = "{error.notNull}")
	@Range(min = -180, max = 180, message = "{error.rangeLonCoordinate}")
	private Double longitude;

	/**
	 * transient value
	 */
	private transient Double distance;

	@Override
	public String getParentId() {
		return parentId;
	}

	@Override
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public String getParentModel() {
		return parentModel;
	}

	@Override
	public void setParentModel(String parentModel) {
		this.parentModel = parentModel;
	}

	@Override
	public Double getLatitude() {
		return latitude;
	}

	@Override
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@Override
	public Double getLongitude() {
		return longitude;
	}

	@Override
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public Double getDistance() {
		return distance;
	}

	@Override
	public void setDistance(Double distance) {
		this.distance = distance;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "distance", "created", "modified", "creator", "modifier");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "distance", "created", "modified", "creator", "modifier");
	}

	@Override
	public String getTitle() {
		return getLatitude() + "/" + getLongitude();
	}
}
