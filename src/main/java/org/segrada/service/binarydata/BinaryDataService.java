package org.segrada.service.binarydata;

import org.segrada.model.prototype.SegradaEntity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
 * File service to save files in a database/filesystem or the like and retrieve them there
 */
public interface BinaryDataService {
	/**
	 * check existence of reference
	 * @param id identifier
	 * @return true of reference exists
	 */
	boolean referenceExists(@Nullable String id);

	/**
	 * remove reference
	 * @param id identifier
	 * @return true of reference has been removed
	 */
	boolean removeReference(@Nullable String id);

	/**
	 * save data as new reference
	 * @param entity reference entity
	 * @param fileName original file name
	 * @param mimeType mime type
	 * @param data representation
	 * @param oldReferenceToReplace id of reference to replace, can be null
	 * @return new identifier
	 */
	String saveNewReference(SegradaEntity entity, String fileName, String mimeType, byte[] data, @Nullable String oldReferenceToReplace);

	/**
	 * update metadata of file with correct id (normally called after save "new")
	 * @param id identifier
	 * @param entity referencing
	 */
	void updateReferenceId(String id, SegradaEntity entity);

	/**
	 * retrieve data as bytes
	 * @param id identifier
	 * @return stream of resource
	 * @throws IOException
	 */
	byte[] getBinaryData(String id) throws IOException;

	/**
	 * retrieve data as stream
	 * @param id identifier
	 * @return stream of resource
	 * @throws IOException
	 */
	InputStream getBinaryDataAsStream(String id) throws IOException;

	/**
	 * retrieve file name for downloading
	 * @param id identifier
	 * @return download file name
	 */
	String getFilename(String id);
}
