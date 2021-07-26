package org.segrada.util;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

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
 * Simple String escape util inspired by deprecated commons method
 */
public final class OrientStringEscape {
	private OrientStringEscape() throws InstantiationException{
		throw new InstantiationException("The class is not created for instantiation");
	}

	/**
	 * <p>Escapes the characters in a <code>String</code> to be suitable to pass to
	 * an SQL query.</p>
	 *
	 * <p>For example,
	 * <pre>statement.executeQuery("SELECT * FROM MOVIES WHERE TITLE='" +
	 *   StringEscapeUtils.escapeSql("McHale's Navy") +
	 *   "'");</pre>
	 * </p>
	 *
	 * <p>At present, this method only turns single-quotes into doubled single-quotes
	 * (<code>"McHale's Navy"</code> => <code>"McHale''s Navy"</code>). It does not
	 * handle the cases of percent (%) or underscore (_) for use in LIKE clauses.</p>
	 *
	 * see http://www.jguru.com/faq/view.jsp?EID=8881
	 * @param str  the string to escape, may be null
	 * @return a new String, escaped for SQL, <code>null</code> if null string input
	 */
	public static String escapeOrientSql(@Nullable String str) {
		if (str == null) {
			return null;
		}
		return StringUtils.replace(str, "'", "\\'");
	}
}
