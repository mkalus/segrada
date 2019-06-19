package org.segrada.service.binarydata;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.session.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.segrada.util.Preconditions.checkNotNull;

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
 * Implementation of the service to save data in the file system
 */
@Singleton
public class BinaryDataServiceFile extends AbstractBinaryDataBaseService {
	private static final Logger logger = LoggerFactory.getLogger(BinaryDataServiceFile.class);

	/**
	 * path where binary data will be saved
	 */
	private final File savePath;

	/**
	 * Constructor
	 */
	@Inject
	public BinaryDataServiceFile(ApplicationSettings applicationSettings) {
		logger.info("BinaryDataServiceFile implemented as BinaryDataService.");

		// construct save path from settings
		String savePathLocal = checkNotNull(applicationSettings.getSetting("savePath"), "savePath");

		if (!savePathLocal.endsWith(File.separator)) savePathLocal += File.separator;
		savePathLocal += "binary" + File.separator;

		this.savePath = new File(savePathLocal);

		createPath();
	}

	@Override
	protected void createPath() {
		if (!savePath.exists() && !savePath.mkdirs()) { // create dirs recursively, if needed
			throw new RuntimeException("Could not create path" + savePath);
		}
	}

	@Override
	public boolean referenceExists(@Nullable String id) {
		return id != null && (new File(savePath, id)).exists();

	}

	@Override
	public boolean removeReference(@Nullable String id) {
		if (id == null || !referenceExists(id)) return false;

		if (logger.isInfoEnabled()) logger.info("Deleting file " + id);

		// delete metadata, too
		(new File(new File(savePath, id) + ".metadata")).delete();

		return (new File(savePath, id)).delete();
	}

	@Override
	public String saveNewReference(SegradaEntity entity, String fileName, String mimeType, byte[] data, @Nullable String oldReferenceToReplace) {
		// create unique resource name
		UniqueResourceName uniqueResourceName = createNewUniqueResourceName(fileName);

		// create temporary filename
		try {
			File myFile = File.createTempFile(uniqueResourceName.prefix, uniqueResourceName.suffix, savePath);

			FileOutputStream fos = new FileOutputStream(myFile);
			fos.write(data);
			fos.close();

			// write metadata to file
			File metadata = new File(myFile.getAbsolutePath() + ".metadata");
			FileOutputStream fileOutputStream = new FileOutputStream(metadata);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);

			outputStreamWriter.write(fileName + "\n" + entity.getModelName() + ":" + entity.getId() + "\n" + mimeType);
			outputStreamWriter.close();

			String newFileReference = myFile.getName();

			// replace old reference, if it has been set
			if (oldReferenceToReplace != null) {
				if (logger.isDebugEnabled())
					logger.info("Replacing old reference " + oldReferenceToReplace + " by " + newFileReference);
				removeReference(oldReferenceToReplace);
			}

			return newFileReference;
		} catch (IOException e) {
			logger.error("Error saving new reference file: " + fileName + " for " + entity.toString() + ".", e);
			return null;
		}
	}

	@Override
	public void updateReferenceId(String id, SegradaEntity entity) {
		if (!referenceExists(id)) return;

		try {
			File metadata = new File(new File(savePath, id) + ".metadata");

			// read metadata
			FileInputStream fileInputStream = new FileInputStream(metadata);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			BufferedReader fileReader = new BufferedReader(inputStreamReader);
			String fileName = fileReader.readLine().trim();
			String oldReference = fileReader.readLine().trim();
			String mimeType = fileReader.readLine().trim();
			fileReader.close();

			// read first line from metadata
			FileOutputStream fileOutputStream = new FileOutputStream(metadata);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
			outputStreamWriter.write(fileName + "\n" + entity.getModelName() + ":" + entity.getId() + "\n" + mimeType);
			outputStreamWriter.close();

			if (logger.isInfoEnabled())
				logger.info("Updated reference id from " + oldReference + " to " + entity.getModelName());
		} catch (IOException e) {
			logger.warn("Could not update metadata for " + id, e);
		}
	}

	@Override
	public byte[] getBinaryData(String id) throws IOException {
		if (!referenceExists(id)) return null;

		FileInputStream fis = new FileInputStream(new File(savePath, id));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		for (int readNum; (readNum = fis.read(buf)) != -1;) {
			bos.write(buf, 0, readNum);
		}

		return bos.toByteArray();
	}

	@Override
	public InputStream getBinaryDataAsStream(String id) throws IOException {
		if (!referenceExists(id)) return null;

		return new FileInputStream(new File(savePath, id));
	}

	@Override
	public String getFilename(String id) {
		if (!referenceExists(id)) return null;

		try {
			// read first line from metadata
			File metadata = new File(new File(savePath, id) + ".metadata");

			FileInputStream fileInputStream = new FileInputStream(metadata);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			BufferedReader fileReader = new BufferedReader(inputStreamReader);
			String fileName = fileReader.readLine().trim();
			fileReader.close();

			return fileName;
		} catch (IOException e) {
			logger.warn("Could not retreive file name from fileReference " + id, e);
			return id;
		}
	}
}
