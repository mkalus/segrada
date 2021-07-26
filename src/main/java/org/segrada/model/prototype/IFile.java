package org.segrada.model.prototype;

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
 * Node model interface
 */
public interface IFile extends SegradaAnnotatedEntity {
	void setTitle(String title);

	String getFilename();
	void setFilename(String filename);

	String getDescription();
	void setDescription(String description);

	String getDescriptionMarkup();
	void setDescriptionMarkup(String descriptionMarkup);

	String getCopyright();
	void setCopyright(String copyright);

	String getFullText();
	void setFullText(String fullText);

	String getMimeType();
	void setMimeType(String mimeType);

	/**
	 * @return file type, typically suffix of file
	 */
	String getFileType();

	String getLocation();
	void setLocation(String location);

	/**
	 * should full text of file be saved in database for search?
	 */
	Boolean getIndexFullText();
	void setIndexFullText(Boolean indexFullText);

	/**
	 * should the file itself be saved in the database?
	 */
	Boolean getContainFile();
	void setContainFile(Boolean containFile);

	/**
	 * @return file size in bytes or 0 if file has not been saved in this record
	 */
	Long getFileSize();
	void setFileSize(Long fileSize);

	String getFileIdentifier();
	void setFileIdentifier(String fileIdentifier);

	String getThumbFileIdentifier();
	void setThumbFileIdentifier(String thumbnailIdentifier);
}
