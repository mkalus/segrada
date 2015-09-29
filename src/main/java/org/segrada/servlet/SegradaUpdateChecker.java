package org.segrada.servlet;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
 * Threaded request for updates of segrada
 */
public class SegradaUpdateChecker {
	private static final Logger logger = LoggerFactory.getLogger(SegradaUpdateChecker.class);

	/**
	 * current version
	 */
	private static final String currentVersion = "v0.0.6";

	private final OrientGraphFactory graph;

	/**
	 * Constructor
	 * @param graph instance of OrientGraphFactory
	 */
	@Inject
	public SegradaUpdateChecker(OrientGraphFactory graph) {
		this.graph = graph;
	}

	/**
	 * check for update
	 */
	public void checkForUpdate() {
		logger.info("Checking Segrada update");

		// create update thread and run it
		UpdateThread updateThread = new UpdateThread();
		updateThread.run();
	}

	private class UpdateThread extends Thread {
		@Override
		public void run() {
			ODatabaseDocumentTx db = null;
			try {
				db = graph.getDatabase();

				// does class Config exist in database?
				if (db.getMetadata().getSchema().existsClass("Config")) {
					// get last update
					// create query
					String sql = "select value from Config where key = 'lastUpdateCheck'";
					// execute query
					OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sql);
					List<ODocument> list = db.command(query).execute();

					// update needed?
					boolean updateNeeded = false;
					if (list.size() == 0) updateNeeded = true;
					else { // update needed?
						ODocument doc = list.get(0);
						try { // try to parse int
							int lastCheck = Integer.parseInt(doc.field("value", String.class));
							int now = (int) (System.currentTimeMillis()/1000);
							// check within the last 24 hours?
							int timePassed = now - lastCheck;
							if (timePassed > 24*60*60) updateNeeded = true;
							else logger.info("No update check needed. Last check " + timePassed + " seconds ago.");

							updateNeeded = true; // TODO: delete
						} catch (Throwable e) {
							logger.warn("UpdateThread could not determine correct lastUpdateCheck although Config class exists in database", e);
							updateNeeded = true;
						}
					}

					if (updateNeeded) {
						logger.info("Update check needed: Checking now.");

						// run update and return value
						String version = null;
						try {
							version = getHTML();
						} catch (Exception e) {
							logger.error("Could not load new version status from http://segrada.org/fileadmin/downloads/version.txt. Skipping update check.");

							// clear version update - just to make sure we have no strange artifacts
							ODocument versionUpdateDoc = new ODocument("Config");
							versionUpdateDoc.field("value", "");
							versionUpdateDoc.field("key", "versionUpdate");
							versionUpdateDoc.save();
						}

						if (version != null) {
							String versionUpdate;
							if (toNumericVersion(currentVersion) < toNumericVersion(version)) {
								if (logger.isInfoEnabled())
									logger.info("UPDATE NEEDED: My version is " + currentVersion + ", server said newest is " + version);
								versionUpdate = version;
							} else {
								logger.info("No update needed.");
								versionUpdate = "";
							}

							// update last check
							ODocument lastUpdateDoc = new ODocument("Config");
							lastUpdateDoc.field("value", Long.toString((System.currentTimeMillis()/1000)));
							lastUpdateDoc.field("key", "lastUpdateCheck");
							lastUpdateDoc.save();

							// update version update
							ODocument versionUpdateDoc = new ODocument("Config");
							versionUpdateDoc.field("value", versionUpdate);
							versionUpdateDoc.field("key", "versionUpdate");
							versionUpdateDoc.save();
						}
					}
				} else logger.info("No configuration yet, no update check.");
			} finally {
				// close db, if not null
				if (db != null)
					db.close();
			}
		}

		/**
		 * load update version from web
		 * @return version
		 * @throws Exception if something went wrong
		 */
		private String getHTML() throws Exception {
			StringBuilder result = new StringBuilder();
			URL url = new URL("http://segrada.org/fileadmin/downloads/version.txt");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			return result.toString();
		}

		/**
		 * convert numeric version to numeric value (for comparisons)
		 * @param version e.g. v1.2.3
		 * @return numeric version e.g. 001002003
		 */
		private int toNumericVersion(String version) {
			if (version == null) return 0;

			int v = 0;

			// cut off "v"
			version = version.substring(1);

			// split between "."
			String parts[] = version.split("\\.");
			if (parts.length != 3) return -1;

			for (int i = 0; i < 3; i++)
				v = v * 100 + Integer.parseInt(parts[i]);

			return v;
		}
	}
}
