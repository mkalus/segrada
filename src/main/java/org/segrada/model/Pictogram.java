package org.segrada.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.IPictogram;

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
 * Pictogram model implementation
 */
public class Pictogram extends AbstractSegradaEntity implements IPictogram {
	private static final long serialVersionUID = 1L;

	/**
	 * list of allowed image mime types
	 * note: list MUST be in alphabetic order because we use binary searches on it
	 */
	public static final String[] ALLOWED_IMAGE_TYPES = new String[]{
			"image/gif",
			"image/jpeg",
			"image/png",
			"image/svg+xml"
	};

	@NotNull(message = "error.notNull")
	@Size(min=2, max=64, message = "error.title.size.2.64")
	private String title = "";

	/**
	 * Reference to data - on upload
	 */
	private transient byte[] data;

	/**
	 * Mime type of data - on upload
	 */
	private transient String mimeType;

	/**
	 * original file name - on upload
	 */
	private transient String fileName;

	/**
	 * reference to file identifier - when loaded from db
	 */
	private String fileIdentifier;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String getFileIdentifier() {
		return fileIdentifier;
	}

	@Override
	public void setFileIdentifier(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "data", "mimeType", "fileName", "created", "modified", "creator", "modifier");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "data", "mimeType", "fileName", "created", "modified", "creator", "modifier");
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jsonObject = super.toJSON();

		try {
			jsonObject.put("title", title);
			jsonObject.put("mimeType", mimeType);
			jsonObject.put("fileName", fileName);
			jsonObject.put("fileIdentifier", fileIdentifier);
		} catch (Exception e) {
			// ignore
		}

		return jsonObject;
	}
}
