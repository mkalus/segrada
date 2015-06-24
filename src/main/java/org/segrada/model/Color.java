package org.segrada.model;

import org.apache.commons.lang3.StringUtils;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.IColor;

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
 * Color model implementation
 */
public class Color extends AbstractSegradaEntity implements IColor {
	private static final long serialVersionUID = 1L;

	/**
	 * Main title
	 */
	@NotNull(message = "{error.notNull}")
	@Size(min=1, message = "{error.notEmpty}")
	private String title;

	/**
	 * Color code in long format
	 */
	@NotNull(message = "{error.notNull}")
	private Integer color;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public Integer getColor() {
		return color;
	}

	@Override
	public void setColor(Integer color) {
		if (color == null || color > 0xffffff || color < 0) color = null; // reset if  invalid
		this.color = color;
	}

	@Override
	public String getColorCode() {
		return (color == null)?"":"#" + StringUtils.leftPad(Long.toHexString(color).toUpperCase(), 6, "0");
	}

	@Override
	public void setColorCode(String colorCode) {
		if (colorCode == null || "".equals(colorCode)) {
			color = null;
		} else {
			// add for decoding below
			if (!colorCode.startsWith("#")) colorCode = "#" + colorCode;
			try {
				color = Integer.decode(colorCode);
			} catch (NumberFormatException e) {
				color = null;
				// fail silently
			}
		}
	}

	@Override
	public int getR() {
		return color==null?0:(int) (color >> 16) & 0x000000FF;
	}
	@Override
	public int getG() {
		return color==null?0:(int) (color >> 8) & 0x000000FF;
	}
	@Override
	public int getB() {
		return color==null?0:(int) (color & 0x000000FF);
	}
}