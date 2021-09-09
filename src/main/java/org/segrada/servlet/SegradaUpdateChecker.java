package org.segrada.servlet;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.segrada.util.OrientStringEscape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
 * Threaded request for updates of segrada
 */
public class SegradaUpdateChecker {
	private static final Logger logger = LoggerFactory.getLogger(SegradaUpdateChecker.class);

	/**
	 * current version
	 */
	public static final String currentVersion = "v0.5.4";

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
		updateThread.start();
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
					if (list.isEmpty()) updateNeeded = true;
					else { // update needed?
						ODocument doc = list.get(0);
						try { // try to parse int
							int lastCheck = Integer.parseInt(doc.field("value", String.class));
							int now = (int) (System.currentTimeMillis()/1000);
							// check within the last 24 hours?
							int timePassed = now - lastCheck;
							if (timePassed > 24*60*60) updateNeeded = true;
							else logger.info("No update check needed. Last check " + timePassed + " seconds ago.");
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
							logger.error("Could not load new version status from https://segrada.org/fileadmin/downloads/version.txt. Skipping update check.");

							// clear version update - just to make sure we have no strange artifacts
							db.command(new OCommandSQL("DELETE FROM Config WHERE key = 'versionUpdate'")).execute();
						}

						if (version != null) {
							String versionUpdate;
							if (toNumericVersion(currentVersion) < toNumericVersion(version)) {
								if (logger.isInfoEnabled())
									logger.info("UPDATE NEEDED: My version is " + currentVersion + ", server said newest is " + version);
								versionUpdate = version;
							} else {
								logger.info("No update needed (version is " + currentVersion + ").");
								versionUpdate = "";
							}

							// upsert last check
							String update = "UPDATE Config SET key = 'lastUpdateCheck', value = '" + Long.toString((System.currentTimeMillis() / 1000)) + "' UPSERT WHERE key = 'lastUpdateCheck'";
							db.command(new OCommandSQL(update)).execute();

							// upsert version update
							if (versionUpdate == null || versionUpdate.isEmpty()) {
								db.command(new OCommandSQL("DELETE FROM Config WHERE key = 'versionUpdate'")).execute();
							} else {
								update = "UPDATE Config SET key = 'versionUpdate', value = '" + OrientStringEscape.escapeOrientSql(versionUpdate) + "' UPSERT WHERE key = 'versionUpdate'";
								db.command(new OCommandSQL(update)).execute();
							}
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
			URL url = new URL("https://segrada.org/fileadmin/downloads/version.txt");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			// relatively short timeouts
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
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

			// cut off "v"
			version = version.substring(1);

			// split between "."
			String[] parts = version.split("\\.");
			if (parts.length != 3) return -1;

			int v = 0;
			for (int i = 0; i < 3; i++)
				v = v * 100 + Integer.parseInt(parts[i]);

			return v;
		}
	}
}
