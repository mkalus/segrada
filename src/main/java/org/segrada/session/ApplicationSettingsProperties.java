package org.segrada.session;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;

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
		buildMap.put("SEGRADA_REQUIRE_LOGIN", "requireLogin");
		buildMap.put("SEGRADA_LUCENE_ANALYZER", "lucene.analyzer");
		buildMap.put("SEGRADA_SERVER_PORT", "server.port");
		buildMap.put("SEGRADA_SERVER_CONTEXT", "server.context");
		buildMap.put("SEGRADA_SOLR_SERVER", "solr.server");
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

		// now overwrite propeties with those defined as system variables
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
