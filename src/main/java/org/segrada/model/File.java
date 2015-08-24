package org.segrada.model;

import org.segrada.model.base.AbstractAnnotatedModel;
import org.segrada.model.prototype.IFile;

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
 * File model implementation
 */
public class File extends AbstractAnnotatedModel implements IFile {
	private static final long serialVersionUID = 1L;

	/**
	 * Main title
	 */
	private String title = "";

	/**
	 * File name
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String filename;

	private String description = "";

	/**
	 * Markup of description field
	 */
	@NotNull(message = "error.notNull")
	private String descriptionMarkup = "default";

	private String copyright = "";

	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String mimeType;

	/**
	 * information on location of file
	 */
	private String location = "";

	/**
	 * file's full text saved in the database
	 */
	private String fullText;

	/**
	 * length of file
	 */
	private Long fileSize = 0L;

	/**
	 * should full text of file be saved in database for search?
	 */
	@NotNull(message = "error.notNull")
	private Boolean indexFullText = true;

	/**
	 * should the file itself be saved in the database?
	 */
	@NotNull(message = "error.notNull")
	private Boolean containFile = true;

	/**
	 * reference to file
	 */
	private String fileIdentifier;

	/**
	 * Reference to data - on upload
	 */
	transient private byte[] data;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
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
	public String getCopyright() {
		return copyright;
	}

	@Override
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String getFullText() {
		return fullText;
	}

	@Override
	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	@Override
	public Long getFileSize() {
		return fileSize;
	}

	@Override
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public Boolean getIndexFullText() {
		return indexFullText;
	}

	@Override
	public void setIndexFullText(Boolean indexFullText) {
		this.indexFullText = indexFullText;
	}

	@Override
	public Boolean getContainFile() {
		return containFile;
	}

	@Override
	public void setContainFile(Boolean containFile) {
		this.containFile = containFile;
	}

	@Override
	public String getFileIdentifier() {
		return fileIdentifier;
	}

	@Override
	public void setFileIdentifier(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "{File}" + (getId() == null ? "*" : getId()) + ", " + getFilename();
	}
}
