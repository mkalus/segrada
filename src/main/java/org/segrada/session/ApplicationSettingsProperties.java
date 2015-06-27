package org.segrada.session;

import com.google.inject.Singleton;

import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

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
	/**
	 * loaded properties
	 */
	private Properties settings = new Properties();

	/**
	 * Constructor
	 */
	public ApplicationSettingsProperties() {
		InputStream input;

		ClassLoader classLoader = getClass().getClassLoader();

		try {
			input = classLoader.getResourceAsStream("application.properties");

			settings.load(input);

			input.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * read settings
	 * @param key to query for
	 * @return string or null
	 */
	@Override
	public String getSetting(String key) {
		return settings.getProperty(key);
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
