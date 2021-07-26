package org.segrada.model;

import org.segrada.model.base.AbstractAnnotatedModel;
import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.ISource;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Source model implementation
 */
public class Source extends AbstractCoreModel implements ISource {
	// we will just use a part of the core model, as defined by ISource:
	// time stuff, map stuff

	private static final long serialVersionUID = 1L;

	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String shortTitle = "";

	private String longTitle = "";

	private String sourceType = "";

	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String shortRef = "";

	private String url = "";

	private String productCode = "";

	private String author = "";

	private String citation = "";

	private String copyright = "";

	private String description = "";

	private String descriptionMarkup = "default";

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	@Override
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public String getLongTitle() {
		return longTitle;
	}

	@Override
	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	@Override
	public String getSourceType() {
		return sourceType;
	}

	@Override
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public String getTitle() {
		return longTitle==null||longTitle.isEmpty()?shortTitle:longTitle;
	}

	@Override
	public String getShortRef() {
		return shortRef;
	}

	@Override
	public void setShortRef(String shortRef) {
		this.shortRef = shortRef;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getProductCode() {
		return productCode;
	}

	@Override
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String getCitation() {
		return citation;
	}

	@Override
	public void setCitation(String citation) {
		this.citation = citation;
	}

	@Override
	public String getCopyright() {
		return copyright;
	}

	@Override
	public void setCopyright(String copyright) {
		this.copyright = copyright;
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
	public String toString() {
		return "{Source}" + (getId() == null ? "*" : getId()) + " [" + getShortRef() + "]: " + getShortTitle();
	}
}
