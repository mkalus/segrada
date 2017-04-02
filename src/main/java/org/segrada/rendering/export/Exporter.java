package org.segrada.rendering.export;

import org.segrada.model.prototype.SegradaEntity;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2017 Maximilian Kalus [segrada@auxnet.de]
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
 * Exporter Interface - export saved queries to some format
 */
public interface Exporter {
	/**
	 * Export data as stream
	 * @param os output stream to write to
	 * @param title of export
	 * @param extractedData list of extracted data
	 */
	void export(OutputStream os, String title, Map<String, List<SegradaEntity>> extractedData);

	/**
	 * @return Media type e.g. application/xml
	 */
	String getMediaType();

	/**
	 * Filename to download
	 * @param id of query
	 * @return filename of downloaded file e.g. 32-0.xml
	 */
	String getFileName(String id);
}
