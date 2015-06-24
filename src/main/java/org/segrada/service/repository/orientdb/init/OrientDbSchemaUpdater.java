package org.segrada.service.repository.orientdb.init;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.segrada.util.PasswordEncoder;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * Called on application start to update schema
 */
public class OrientDbSchemaUpdater {
	private final static Logger logger = Logger.getLogger(OrientDbSchemaUpdater.class.getName());

	/**
	 * current version of db
	 */
	private static final int CURRENT_VERSION = 1;

	/**
	 * graph factory instance
	 */
	private OrientGraphFactory orientGraphFactory;

	private final String dbPath;

	/**
	 * keep config version
	 */
	private int version = -1;

	/**
	 * Constructor
	 * @param orientGraphFactory graph factory instance
	 * @param dbPath e.g. plocal:/tmp/segrada
	 */
	public OrientDbSchemaUpdater(OrientGraphFactory orientGraphFactory, String dbPath) {
		this.orientGraphFactory = orientGraphFactory;
		this.dbPath = dbPath;
	}

	/**
	 * get version from db and save it in instance variable
	 * @return current version (0 = not set)
	 */
	private int getConfigVersion() {
		// initialize
		if (version < 0) {
			// open database
			ODatabaseDocumentTx db = orientGraphFactory.getDatabase();

			// does class Config exist in database?
			if (db.getMetadata().getSchema().existsClass("Config")) {
				// get version
				// create query
				String sql = "select value from Config where key = 'version'";
				// execute query
				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sql);
				List<ODocument> list = db.command(query).execute();

				// found something in db?
				if (list.size() == 0) version = 0;
				else { // yes
					ODocument doc = list.get(0);
					try { // try to parse int
						version = Integer.parseInt((String) doc.field("value", String.class));
					} catch (Throwable e) {
						logger.warning("getConfigVersion could not determine correct version although Config class exists in database");
						version = 0;
					}
				}
			} else { // no -> virgin db
				version = 0;
			}

			// close db
			db.close();
		}

		return version;
	}

	/**
	 * create database if it does not exist yet
	 */
	public void initializeDatabase() {
		if (dbPath.startsWith("remote:")) {
			logger.info("Cannot check remote database existence for " + dbPath + ": skipping.");
			return;
		}

		if (!orientGraphFactory.exists()) {
			logger.warning("Database " + dbPath + " did not exist: Creating it now.");
			try {
				OrientGraph graph = new OrientGraph(dbPath);
				graph.shutdown();
			} catch (Exception e) {
				throw new RuntimeException("Could not initialize database in " + dbPath + ": " + e.getMessage());
			}
		}
	}

	/**
	 * build and/or update my schema
	 */
	public void buildOrUpdateSchema() {
		// get version of database
		int version = getConfigVersion();

		// no version update needed => just return
		if (version >= CURRENT_VERSION) return;

		// open database
		ODatabaseDocumentTx db = orientGraphFactory.getDatabase();

		// run schema update scripts per version number
		for (int i = version; i < CURRENT_VERSION; i++)
			runSchemaScript("/orientdb/schema_version_" + i + ".sql", db);

		// close db
		db.close();
	}

	/**
	 * helper to update database per schema script step by step
	 * @param resourceName name of update script
	 * @param db instance
	 */
	private void runSchemaScript(String resourceName, ODatabaseDocumentTx db) {
		// read schema from resource file
		InputStream is = this.getClass().getResourceAsStream(resourceName);
		// if no resouce exists do not run updater
		try {
			if (is.available() == 0) return;
		} catch (IOException e) {
			return;
		}

		logger.info("Running sql update " + resourceName + " to update schema now.");

		// read sql lines and update
		try {
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			while((line = in.readLine()) != null) {
				if (!line.equals("") && !line.startsWith("#"))
					try {
						db.command(new OCommandSQL(line)).execute();
					} catch (Exception e) {
						logger.log(Level.WARNING, "Exception in schema update while executing \"" + line + "\": " + e.getMessage());
					} catch (Error e) {
						logger.log(Level.SEVERE, "Error in schema update while executing \"" + line + "\": " + e.getMessage());
					}
			}

			in.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO Exception while reading schema file " + resourceName + ": " + e.getMessage());
		}
	}

	/**
	 * populate with (default) data or update existing data (mostly config)
	 * @param passwordEncoder encoder to create new admin user
	 */
	public void populateWithData(@Nullable PasswordEncoder passwordEncoder) {
		// get version of database
		int version = getConfigVersion();

		// open database
		ODatabaseDocumentTx db = orientGraphFactory.getDatabase();

		// no version update needed => just return
		if (version >= CURRENT_VERSION) return;

		// version 0 => 1
		if (version <= 0) {
			// create default password
			String password = "password";
			if (passwordEncoder != null)
				password = passwordEncoder.encode(password);

			long now = System.currentTimeMillis();

			// create default admin user
			ODocument doc = new ODocument("User")
					.field("login", "admin")
					.field("password", password)
					.field("name", "Administrator")
					.field("role", "ADMIN")
					.field("created", now)
					.field("modified", now)
							//.field("lastLogin", null)
					.field("active", true);
			db.save(doc);

			// create default color entries
			String[] defaultColors = {"#ac725e", "#d06b64", "#f83a22", "#fa573c", "#ff7537", "#ffad46", "#42d692",
					"#16a765", "#7bd148", "#b3dc6c", "#fbe983", "#fad165", "#92e1c0", "#9fe1e7", "#9fc6e7", "#4986e7",
					"#9a9cff", "#b99aff", "#c2c2c2", "#cabdbf", "#cca6ac", "#f691b2", "#cd74e6", "#a47ae2"};
			for (String color : defaultColors) {
				doc = new ODocument("Color")
						.field("title", color)
						.field("color", Long.decode(color));
				db.save(doc);
			}

			// version bump to 1
			version = 1;

			logger.info("Schema data updated to version 1.");
		}


		// create config defaults
		db.save(new ODocument("Config")
				.field("key", "version")
				.field("value", Integer.toString(version)));

		// copy local version to instance version
		this.version = version;

		logger.info("Schema update finished: Version number of database is now " + version);

		// close db
		db.close();
	}
}
