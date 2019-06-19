package org.segrada.service.binarydata;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.DFSInputStream;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.session.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

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
 * Implementation of the service to save data into a Hadoop cluster
 */
@Singleton
public class BinaryDataServiceHadoop extends AbstractBinaryDataBaseService {
	private static final Logger logger = LoggerFactory.getLogger(BinaryDataServiceHadoop.class);

	/**
	 * dfs client
	 */
	private final DFSClient client;


	/**
	 * root path to save Segrada stuff in
	 */
	private final String rootPath;

	/**
	 * Constructor
	 */
	@Inject
	public BinaryDataServiceHadoop(ApplicationSettings applicationSettings) throws IOException, URISyntaxException {
		logger.info("BinaryDataServiceHadoop implemented as BinaryDataService.");

		Configuration conf = new Configuration();
		// get configuration files from settings
		String files = applicationSettings.getSetting("binaryDataService.hadoop.configurationFiles");
		if (files != null && !files.isEmpty()) {
			for (String file : files.split(",")) {
				conf.addResource(file.trim()); // remove whitespace around filename
			}
		}
		// override settings
		String uri = applicationSettings.getSetting("binaryDataService.hadoop.fs.defaultFS");
		if (uri != null && !uri.isEmpty())
			conf.set("fs.defaultFS", uri);
		else uri = "hdfs://localhost:9000/";

		String userName = applicationSettings.getSetting("binaryDataService.hadoop.userName");
		if (userName != null && !userName.isEmpty())
			System.setProperty("HADOOP_USER_NAME", userName);

		String path = applicationSettings.getSetting("binaryDataService.hadoop.path");
		if (path == null || path.isEmpty()) {
			logger.warn("binaryDataService.hadoop.path is empty, fallback to default segrada");
			path = "/segrada/";
		}
		// fix slashes
		if (!path.endsWith("/")) path += "/";
		if (!path.startsWith("/")) path = "/" + path;
		rootPath = path; //assign once

		// create and connect client
		client = new DFSClient(new URI(uri), conf);

		if (logger.isInfoEnabled())
			logger.info("BinaryDataServiceHadoop connected to " + uri);

		// try to create path - if this fails we know that something went wrong
		createPath();
	}

	@Override
	protected void createPath() throws IOException {
		if (!client.exists(rootPath) && !client.mkdirs(rootPath, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)) {
			logger.warn("Could not create root path " + rootPath);
		}
	}

	@Override
	public boolean referenceExists(@Nullable String id) {
		try {
			return id != null && client.exists(rootPath + id);
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean removeReference(@Nullable String id) {
		if (id == null || !referenceExists(id)) return false;

		if (logger.isInfoEnabled()) logger.info("Deleting file " + id);

		try {
			// delete metadata, too
			client.delete(rootPath + id + ".metadata", false);

			return client.delete(rootPath + id, false);
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			return false;
		}
	}

	@Override
	public String saveNewReference(SegradaEntity entity, String fileName, String mimeType, byte[] data, @Nullable String oldReferenceToReplace) {
		// create unique resource name
		UniqueResourceName uniqueResourceName = createNewUniqueResourceName(fileName);

		// upload file data
		String myFile = rootPath + uniqueResourceName.getFileName();

		try {
			OutputStream out = client.create(myFile, true);
			InputStream in = new BufferedInputStream(new ByteArrayInputStream(data));

			byte[] b = new byte[1024];
			int numBytes = 0;
			while ((numBytes = in.read(b)) > 0) {
				out.write(b, 0, numBytes);
			}
			// Close all the file descriptors
			in.close();
			out.close();

			// write meta data file
			String metaData = myFile + ".metadata";
			byte[] metaDataContent = (fileName + "\n" + entity.getModelName() + ":" + entity.getId() + "\n" + mimeType).getBytes(StandardCharsets.UTF_8);

			out = client.create(metaData, true);
			in = new BufferedInputStream(new ByteArrayInputStream(metaDataContent));
			b = new byte[1024];

			while ((numBytes = in.read(b)) > 0) {
				out.write(b, 0, numBytes);
			}
			// Close all the file descriptors
			in.close();
			out.close();

			// replace old reference, if it has been set
			if (oldReferenceToReplace != null) {
				if (logger.isDebugEnabled())
					logger.info("Replacing old reference " + oldReferenceToReplace + " by " + myFile);
				removeReference(oldReferenceToReplace);
			}

			// return file without path
			return uniqueResourceName.getFileName();
		} catch (IOException e) {
			logger.error("Error saving new reference file into Hadoop: " + fileName + " for " + entity.toString() + ".", e);
			return null;
		}
	}

	@Override
	public void updateReferenceId(String id, SegradaEntity entity) {

	}

	@Override
	public byte[] getBinaryData(String id) throws IOException {
		if (!referenceExists(id)) return null;

		DFSInputStream fis = client.open(rootPath + id);
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

		return client.open(rootPath + id);
	}

	@Override
	public String getFilename(String id) {
		if (!referenceExists(id)) return null;

		try {
			DFSInputStream fis = client.open(rootPath + id + ".metadata");

			BufferedReader fileReader = new BufferedReader(new InputStreamReader(fis));
			String fileName = fileReader.readLine().trim();
			fileReader.close();

			return fileName;
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			return id;
		}
	}
}
