package org.segrada.service.binarydata;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;

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
 * Abstract base class for binary data services
 */
abstract public class AbstractBinaryDataBaseService implements BinaryDataService {
	/**
	 * create save path - should be called by constructor
	 */
	abstract protected void createPath() throws IOException;

	/**
	 * create a new unique resource name
	 * @param fileName original resource file name
	 * @return UniqueResourceName
	 */
	protected UniqueResourceName createNewUniqueResourceName(String fileName) {
		// create new filename, if empty
		if (fileName == null || fileName.isEmpty()) fileName = RandomStringUtils.randomAlphanumeric(8);

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

		// to lower case and clean file name
		suffix = suffix.toLowerCase();
		prefix = prefix.toLowerCase().replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");

		// too short? Make it longer!
		int prefixLen = prefix.length();
		if (prefixLen <= 5) prefix += RandomStringUtils.randomAlphanumeric(5-prefixLen);

		// check if another resource exists with the same name, if yes, add random string
		String prefixAddition = "";
		while (referenceExists(prefix + prefixAddition + suffix)) {
			prefixAddition = "_" + RandomStringUtils.randomNumeric(5);
		}

		return new UniqueResourceName(prefix, suffix);
	}

	/**
	 * helper class to keep unique resource names
	 */
	protected class UniqueResourceName {
		public final String prefix;
		public final String suffix;

		/**
		 * Constructor
		 * @param prefix of filename
		 * @param suffix of filename
		 */
		public UniqueResourceName(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
		}

		public String getFileName() {
			return prefix + suffix;
		}
	}
}
