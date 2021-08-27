package org.segrada.test;

import org.jetbrains.annotations.Nullable;
import org.segrada.session.ApplicationSettings;

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
 * Test application settings OrientDB
 */
public class OrientDbTestApplicationSettings implements ApplicationSettings {
	@Override
	public String getSetting(String key) {
		return "admin";
	}

	@Nullable
	@Override
	public String getSettingOrDefault(String key, String defaultValue) {
		return null;
	}

	@Override
	public int getSettingAsInteger(String key, int defaultValue) {
		return 0;
	}

	@Override
	public double getSettingAsDouble(String key, double defaultValue) {
		return 0;
	}

	@Override
	public Map<String, String> getAllSettingsStartingWith(String key) {
		return null;
	}

	@Override
	public String getSetting(String key, String defaultValue) {
		if (defaultValue != null && !defaultValue.isEmpty()) return defaultValue;
		return "admin";
	}

	@Override
	public void setSetting(String key, String newValue) {
		//ignore
	}

	@Override
	public Collection<String> getKeys() {
		return null;
	}
}
