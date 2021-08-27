package org.segrada.session;

import com.google.inject.Singleton;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;

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
 * Holds application settings
 */
@Singleton
public class ApplicationSettingsProperties implements ApplicationSettings {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationSettingsProperties.class);

	/**
	 * singleton reference
	 */
	private static ApplicationSettings instance;
	
	/**
	 * holds mapping of environmental variables to properties
	 */
	private static final Map<String, String> environmentToProperty;
	static {
		Map<String, String> buildMap = new HashMap<>();
		buildMap.put("SEGRADA_SAVE_PATH", "savePath");
		buildMap.put("SEGRADA_UPLOADS_STORAGE", "uploads.storage");
		buildMap.put("SEGRADA_UPLOADS_MAX_SIZE", "uploads.maximum_upload_size");
		buildMap.put("SEGRADA_ORIENTDB_URL", "orientDB.url");
		buildMap.put("SEGRADA_ORIENTDB_LOGIN", "orientDB.login");
		buildMap.put("SEGRADA_ORIENTDB_PASSWORD", "orientDB.password");
		buildMap.put("SEGRADA_ORIENTDB_REMOTE_ROOT", "orientDB.remote_root");
		buildMap.put("SEGRADA_ORIENTDB_REMOTE_PASSWORD", "orientDB.remote_pw");
		buildMap.put("SEGRADA_REQUIRE_LOGIN", "requireLogin");
		buildMap.put("SEGRADA_LUCENE_ANALYZER", "lucene.analyzer");
		buildMap.put("SEGRADA_SERVER_PORT", "server.port");
		buildMap.put("SEGRADA_SERVER_CONTEXT", "server.context");
		buildMap.put("SEGRADA_SOLR_SERVER", "solr.server");
		buildMap.put("SEGRADA_SOLR_FIELD_ID", "solr.field_id");
		buildMap.put("SEGRADA_SOLR_FIELD_CLASS_NAME", "solr.field_className");
		buildMap.put("SEGRADA_SOLR_FIELD_TITLE", "solr.field_title");
		buildMap.put("SEGRADA_SOLR_FIELD_CONTENT", "solr.field_content");
		buildMap.put("SEGRADA_SOLR_FIELD_TAG", "solr.field_tag");
		buildMap.put("SEGRADA_SOLR_FIELD_COLOR", "solr.field_color");
		buildMap.put("SEGRADA_SOLR_FIELD_ICON", "solr.icon");
		buildMap.put("SEGRADA_MAP_DEFAULT_ZOOM", "map.defaultZoom");
		buildMap.put("SEGRADA_MAP_DEFAULT_LAT", "map.defaultLat");
		buildMap.put("SEGRADA_MAP_DEFAULT_LNG", "map.defaultLng");
		buildMap.put("SEGRADA_MAP_PROVIDER", "map.provider");
		// map options are handled below - as JSON string
		buildMap.put("SEGRADA_BINARY_DATA_SERVICE", "binaryDataService");
		buildMap.put("SEGRADA_HADOOP_CONFIGURATION_FILES", "binaryDataService.hadoop.configurationFiles");
		buildMap.put("SEGRADA_HADOOP_FS_DEFAULT_FS", "binaryDataService.hadoop.fs.defaultFS");
		buildMap.put("SEGRADA_HADOOP_PATH", "binaryDataService.hadoop.path");
		//buildMap.put("HADOOP_USER_NAME", "binaryDataService.hadoop.userName");
		environmentToProperty = Collections.unmodifiableMap(buildMap);
	}

	/**
	 * loaded properties
	 */
	private Properties settings = new Properties();

	/**
	 * Constructor
	 */
	public ApplicationSettingsProperties() {
		// singleton reference
		instance = this;

		InputStream input;

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try {
			input = classLoader.getResourceAsStream("application.properties");

			settings.load(input);

			input.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// now overwrite properties with those defined as system variables
		Map<String, String> var = System.getenv();
		for (Map.Entry<String, String> envToPropEntry : environmentToProperty.entrySet()) {
			if (var.containsKey(envToPropEntry.getKey())) {
				// get value from environment and mapped property name
				String value = var.get(envToPropEntry.getKey());
				String key = envToPropEntry.getValue();
				// set property
				if (value != null) {
					settings.setProperty(key, value);
					if (logger.isInfoEnabled())
						logger.info("Property set from environment: " + key + ": " + value);
				}
			}
		}

		// map options as JSON string
		if (var.containsKey("SEGRADA_MAP_OPTIONS")) {
			String value = var.get("SEGRADA_MAP_OPTIONS");

			try {
				JSONObject jsonSettings = new JSONObject(value);

				Iterator it = jsonSettings.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					settings.setProperty("map.options." + key, jsonSettings.getString(key));
				}
			} catch (Exception e) {
				logger.error("Could not read SEGRADA_MAP_OPTIONS: ", e);
			}
		}

		// now overwrite properties with those defined via command line
		Properties env = System.getProperties();

		for (String key : settings.stringPropertyNames()) {
			String value = env.getProperty(key);
			if (value != null) {
				settings.setProperty(key, value);
				if (logger.isInfoEnabled())
					logger.info("Property set from command line: " + key + ": " + value);
			}
		}
	}

	/**
	 * singleton getter
	 * @return
	 */
	public static ApplicationSettings getInstance() {
		if (instance == null) instance = new ApplicationSettingsProperties();
		return instance;
	}

	/**
	 * read settings
	 * @param key to query for
	 * @return string or null
	 */
	@Nullable
	@Override
	public String getSetting(String key) {
		return settings.getProperty(key);
	}

	/**
	 * retrieve a setting from settings - if empty, get default
	 * @param key to look for
	 * @return value retrieved or default value
	 */
	@Nullable
	@Override
	public String getSettingOrDefault(String key, String defaultValue) {
		String back = getSetting(key);
		if (back == null || back.isEmpty()) return defaultValue;
		return back;
	}

	/**
	 * retrieve a setting from settings - parse to int
	 * @param key to look for
	 * @param defaultValue value retrieved or default value
	 * @return value retrieved or default value
	 */
	@Override
	public int getSettingAsInteger(String key, int defaultValue) {
		String back = getSetting(key);
		if (back == null || back.isEmpty()) return defaultValue;

		try {
			return Integer.parseInt(back);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * retrieve a setting from settings - parse to double
	 * @param key to look for
	 * @param defaultValue value retrieved or default value
	 * @return value retrieved or default value
	 */
	@Override
	public double getSettingAsDouble(String key, double defaultValue) {
		String back = getSetting(key);
		if (back == null || back.isEmpty()) return defaultValue;

		try {
			return Double.parseDouble(back);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Map<String, String> getAllSettingsStartingWith(String key) {
		Map<String, String> filteredSettings = new HashMap<>();

		for (String propertyKey : settings.stringPropertyNames()) {
			if (propertyKey.startsWith(key)) {
				filteredSettings.put(propertyKey.substring(key.length()), settings.get(propertyKey).toString());
			}
		}

		return filteredSettings;
	}

	@Nullable
	@Override
	public String getSetting(String key, @Nullable String defaultValue) {
		String value = getSetting(key);
		return value==null||value.isEmpty()?defaultValue:value;
	}

	@Override
	public void setSetting(String key, String newValue) {
		settings.setProperty(key, newValue);
	}

	@Override
	public Collection<String> getKeys() {
		return settings.stringPropertyNames();
	}
}
