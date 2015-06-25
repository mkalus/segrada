package org.segrada.service.binarydata;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.session.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;

import static org.segrada.util.Preconditions.checkNotNull;

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
 * Implementation of the service to save data in the file system
 */
@Singleton
public class BinaryDataServiceFile implements BinaryDataService {
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
		String savePath = checkNotNull(applicationSettings.getSetting("savePath"), "savePath");

		if (!savePath.endsWith(File.separator)) savePath += File.separator;
		savePath += "binary" + File.separator;

		this.savePath = new File(savePath);

		createPath();
	}

	/**
	 * create path to save stuff in
	 */
	private void createPath() {
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
		if (!referenceExists(id)) return false;

		if (logger.isInfoEnabled()) logger.info("Deleting file " + id);

		// delete metadata, too
		(new File(new File(savePath, id) + ".metadata")).delete();

		return (new File(savePath, id)).delete();
	}

	@Override
	public String saveNewReference(SegradaEntity entity, String fileName, String mimeType, byte[] data, @Nullable String oldReferenceToReplace) {
		// create new filename, if empty
		if (fileName == null || fileName.length() == 0) fileName = RandomStringUtils.randomAlphanumeric(8);

		// create prefix and suffix
		String prefix, suffix;
		int pos = fileName.lastIndexOf(".");
		if (pos == -1) {
			prefix = fileName;
			suffix = ".bin";
		} else {
			prefix = fileName.substring(0, pos);
			suffix = fileName.substring(pos);
			if (suffix.length() <= 1) suffix = ".bin";
		}

		// too short? Make it longer!
		int prefixLen = prefix.length();
		if (prefixLen <= 5) prefix += RandomStringUtils.randomAlphanumeric(5-prefixLen);

		// create temporary filename
		try {
			File myFile = File.createTempFile(prefix, suffix, savePath);

			FileOutputStream fos = new FileOutputStream(myFile);
			fos.write(data);
			fos.close();

			// write metadata to file
			File metadata = new File(myFile.getAbsolutePath() + ".metadata");
			FileWriter fileWriter = new FileWriter(metadata);
			fileWriter.write(fileName + "\n" + entity.getModelName() + ":" + entity.getId() + "\n" + mimeType);
			fileWriter.close();

			String newFileReference = myFile.getName();

			// replace old reference, if it has been set
			if (oldReferenceToReplace != null) {
				if (logger.isDebugEnabled())
					logger.info("Replacing old reference " + oldReferenceToReplace + " by " + newFileReference);
				removeReference(oldReferenceToReplace);
			}

			return newFileReference;
		} catch (IOException e) {
			logger.error("Error saving new reference file: " + fileName + " for " + entity.toString() + ": " + e.getMessage());
			return null;
		}
	}

	@Override
	public void updateReferenceId(String id, SegradaEntity entity) {
		if (!referenceExists(id)) return;

		try {
			File metadata = new File(new File(savePath, id) + ".metadata");

			// read metadata
			BufferedReader fileReader = new BufferedReader(new FileReader(metadata));
			String fileName = fileReader.readLine().trim();
			String oldReference = fileReader.readLine().trim();
			String mimeType = fileReader.readLine().trim();
			fileReader.close();

			// read first line from metadata
			FileWriter fileWriter = new FileWriter(metadata);
			fileWriter.write(fileName + "\n" + entity.getModelName() + ":" + entity.getId() + "\n" + mimeType);
			fileWriter.close();

			if (logger.isInfoEnabled())
				logger.info("Updated reference id from " + oldReference + " to " + entity.getModelName());
		} catch (IOException e) {
			logger.warn("Could not update metadata for " + id + ": " + e.getMessage());
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

			BufferedReader fileReader = new BufferedReader(new FileReader(metadata));
			String fileName = fileReader.readLine().trim();
			fileReader.close();

			return fileName;
		} catch (IOException e) {
			logger.warn("Could not retreive file name from fileReference " + id + ": " + e.getMessage());
			return id;
		}
	}
}
