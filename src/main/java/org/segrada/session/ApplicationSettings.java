package org.segrada.session;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

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
 * Abstract holder of application settings
 */
public interface ApplicationSettings {
	/**
	 * retrieve a setting from the settings
	 * @param key to look for
	 * @return value retrieved or null
	 */
	@Nullable String getSetting(String key);

	/**
	 * retrieve a setting from settings - if empty, get default
	 * @param key to look for
	 * @return value retrieved or default value
	 */
	@Nullable String getSettingOrDefault(String key, String defaultValue);

	/**
	 * retrieve a setting from settings - parse to int
	 * @param key to look for
	 * @param defaultValue value retrieved or default value
	 * @return value retrieved or default value
	 */
	int getSettingAsInteger(String key, int defaultValue);

	/**
	 * retrieve a setting from settings - parse to double
	 * @param key to look for
	 * @param defaultValue value retrieved or default value
	 * @return value retrieved or default value
	 */
	double getSettingAsDouble(String key, double defaultValue);

	/**
	 * retrieve all settings starting with a certain key - this key is also cut from the settings
	 * @param key part to look for
	 * @return list of matches
	 */
	Map<String, String> getAllSettingsStartingWith(String key);

	/**
	 * retrieve a setting from the settings - with default value
	 * @param key to look for
	 * @param defaultValue to set if setting is null or empty
	 * @return value retrieved or default (may be null)
	 */
	@Nullable String getSetting(String key, @Nullable String defaultValue);

	/**
	 * persist setting
	 * @param key to look for
	 * @param newValue new value to be saved
	 */
	void setSetting(String key, String newValue);

	/**
	 * Get all keys
	 * @return all keys
	 */
	Collection<String> getKeys();
}