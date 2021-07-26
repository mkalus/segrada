package org.segrada.util;

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
 * Formats numbers to correct file sizes
 */
public class NumberFormatter {
	/**
	 * see http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java/3758880
	 * @param bytes input bytes
	 * @param si use SI units instead of binary base units
	 * @return string represenation
	 */
	public String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * SI unit output
	 * @param bytes
	 * @return
	 */
	public String fileSizeSI(long bytes) {
		return humanReadableByteCount(bytes, true);
	}

	/**
	 * binary output
	 * @param bytes
	 * @return
	 */
	public String fileSizeBinary(long bytes) {
		return humanReadableByteCount(bytes, false);
	}
}
