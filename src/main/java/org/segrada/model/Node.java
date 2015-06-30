package org.segrada.model;

import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.INode;

import javax.validation.constraints.NotNull;
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
 * Node model implementation
 */
public class Node extends AbstractCoreModel implements INode {
	private static final long serialVersionUID = 1L;

	/**
	 * Main title
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String title = "";

	/**
	 * Alternative titles
	 */
	@NotNull(message = "error.notNull")
	private String alternativeTitles = "";

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
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getAlternativeTitles() {
		return alternativeTitles;
	}

	@Override
	public void setAlternativeTitles(String alternativeTitles) {
		this.alternativeTitles = alternativeTitles;
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
}
