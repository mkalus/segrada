package org.segrada.model;

import org.segrada.model.base.AbstractAnnotatedModel;
import org.segrada.model.prototype.IComment;

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
 * Comment model implementation
 */
public class Comment extends AbstractAnnotatedModel implements IComment {
	private static final long serialVersionUID = 1L;

	/**
	 * Text
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String text = "";

	/**
	 * Markup type of text - see org.segrada.MarkupFilter
	 */
	@NotNull(message = "error.notNull")
	private String markup = "html";

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getMarkup() {
		return markup;
	}

	@Override
	public void setMarkup(String markup) {
		this.markup = markup;
	}

	@Override
	public String getTitle() {
		String startOfText = getText();
		if (startOfText==null) startOfText = "[empty]";
		else if (startOfText.length() > 50) startOfText = startOfText.substring(0, 50) + "…";

		return startOfText;
	}
}
