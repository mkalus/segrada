package org.segrada.model.base;

import org.apache.commons.lang3.StringUtils;
import org.segrada.model.prototype.IPictogram;
import org.segrada.model.prototype.SegradaColoredEntity;

import javax.annotation.Nullable;

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
 * Abstract model that extends AbstractCreatedModifiedModel and keeps color, tags and pictogram data
 */
abstract public class AbstractColoredModel extends AbstractSegradaEntity implements SegradaColoredEntity {
	/**
	 * Pictogram for myself
	 */
	private IPictogram pictogram;

	/**
	 * Color representation
	 */
	private Integer color;

	@Override
	public @Nullable IPictogram getPictogram() {
		return pictogram;
	}

	@Override
	public void setPictogram(IPictogram pictogram) {
		this.pictogram = pictogram;
	}

	@Override
	public @Nullable Integer getColor() {
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
}
