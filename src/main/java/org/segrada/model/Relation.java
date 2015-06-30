package org.segrada.model;

import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.IRelationType;
import org.segrada.model.prototype.SegradaEntity;

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
 * Relation model implementation
 */
public class Relation extends AbstractCoreModel implements IRelation {
	private static final long serialVersionUID = 1L;

	/**
	 * reference to relation type
	 */
	@NotNull(message = "error.notNull")
	private IRelationType relationType;

	/**
	 * from entity
	 */
	@NotNull(message = "error.notNull")
	protected SegradaEntity fromEntity;

	/**
	 * to entity
	 */
	@NotNull(message = "error.notNull")
	protected SegradaEntity toEntity;

	/**
	 * Text
	 */
	@NotNull(message = "error.notNull")
	private String description = "";

	/**
	 * Markup type of text - see org.segrada.MarkupFilter
	 */
	@NotNull(message = "error.notNull")
	private String descriptionMarkup = "html";

	@Override
	public IRelationType getRelationType() {
		return relationType;
	}

	@Override
	public void setRelationType(IRelationType relationType) {
		this.relationType = relationType;
	}

	@Override
	public SegradaEntity getFromEntity() {
		return fromEntity;
	}

	@Override
	public void setFromEntity(SegradaEntity fromEntity) {
		this.fromEntity = fromEntity;
	}

	@Override
	public SegradaEntity getToEntity() {
		return toEntity;
	}

	@Override
	public void setToEntity(SegradaEntity toEntity) {
		this.toEntity = toEntity;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescriptionMarkup() {
		return descriptionMarkup;
	}

	@Override
	public void setDescriptionMarkup(String descriptionMarkup) {
		this.descriptionMarkup = descriptionMarkup;
	}

	@Override
	public String getTitle() {
		return (getFromEntity()==null?"?":getFromEntity().getTitle())
				+ "⇒" + (getRelationType()==null?"?":getRelationType().getFromTitle())
				+ "⇒" + (getToEntity()==null?"?":getToEntity().getTitle());
	}

	@Override
	public String toString() {
		return "{Relation}" + (getId() == null ? "*" : getId()) + ", "
				+ (getFromEntity()==null?"?":getFromEntity().toString())
				+ "⇒" + (getRelationType()==null?"?":getRelationType().getFromTitle())
				+ "⇒" + (getToEntity()==null?"?":getToEntity().toString());
	}
}
