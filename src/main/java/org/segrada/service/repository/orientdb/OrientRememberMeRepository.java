package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.segrada.service.repository.RememberMeRepository;
import org.segrada.util.OrientStringEscape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
 * Repository for RememberMe Services OrientDB implementation
 */
@RequestScoped
public class OrientRememberMeRepository implements RememberMeRepository {
	private static final Logger logger = LoggerFactory.getLogger(OrientRememberMeRepository.class);

	/**
	 * database instance
	 */
	protected final ODatabaseDocumentTx db;

	/**
	 * Constructor
	 */
	@Inject
	public OrientRememberMeRepository(ODatabaseDocumentTx db) {
		this.db = db;
	}

	@Override
	public String createTokenForCookie(String userId) {
		// sanity check: empty?
		if (userId == null || userId.isEmpty()) {
			logger.error("User Id " + userId + " is empty");
			return null;
		}

		// user exists and active?
		ORecordId oid = new ORecordId(userId);
		if (!oid.isValid()) {
			logger.error("User Id " + userId + " is not valid");
			return null;
		}
		ODocument user = db.getRecord(oid);
		if (user == null || !user.getClassName().equals("User")) {
			logger.error("User with id " + userId + " not found");
			return null;
		}
		Boolean active = user.field("active", Boolean.class);
		if (active == null || !active) {
			logger.error("User with id " + userId + " not active");
			return null;
		}

		// ok, all clear - create new token
		String token = UUID.randomUUID().toString().replaceAll("-", "");
		String hashedToken = null;
		// hash token
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			hashedToken = Hex.encodeHexString(md.digest(token.getBytes("UTF-8")));
		} catch (Exception e) {
			logger.error("Could not SHA-256 algorith: keeping tokens in unsecure clear text format!");
			hashedToken = token;
		}

		// also create selector
		String selector = null;
		do {
			selector = RandomStringUtils.randomAlphanumeric(12);
			// does this selector exist in database?
			// tags pointing to document still exist?
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from RememberMeToken where selector = ?");
			List<ODocument> result = db.command(query).execute(selector);
			if (result != null && !result.isEmpty())
				selector = null; // exists: start again!
		} while (selector == null);

		ODocument document = new ODocument("RememberMeToken");
		document.field("user", user);
		document.field("selector", selector);
		document.field("token", hashedToken);
		document.field("expires", new DateTime().plusDays(30).toDate());
		document.save();

		// return token and selector
		return selector.concat(":").concat(token);
	}

	@Override
	public boolean removeToken(String token) {
		// check validity
		Token t = new Token(token);
		if (!t.isValid()) {
			logger.error("Token " + token + " is not valid");
			return false;
		}

		// find token
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from RememberMeToken where selector = ?");
		List<ODocument> result = db.command(query).execute(t.selector);

		if (result == null || result.size() != 1) {
			logger.error("Token " + token + " is not found");
			return false;
		}

		ODocument document = result.get(0);

		// token valid?
		if (!t.token.equals(document.field("token", String.class))) {
			logger.error("Token " + token + " access error");
			return false;
		}

		// ok, now remove token
		String commandQuery = "DELETE FROM RememberMeToken WHERE selector = '" + OrientStringEscape.escapeOrientSql(t.selector) + "'";
		db.command(new OCommandSQL(commandQuery)).execute();

		return true;
	}

	@Override
	public String validateTokenAndGetUserId(String token) {
		// check validity
		Token t = new Token(token);
		if (!t.isValid())  {
			logger.error("Token " + token + " is not valid");
			return null;
		}

		// find token
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from RememberMeToken where selector = ?");
		List<ODocument> result = db.command(query).execute(t.selector);

		if (result == null || result.size() != 1) {
			logger.error("Token " + token + " is not found");
			return null;
		}

		ODocument document = result.get(0);

		// token valid?
		if (!t.token.equals(document.field("token", String.class))) {
			logger.error("Token " + token + " access error");
			return null;
		}

		// date is still valid?
		Date date = document.field("expires", Date.class);
		if (!date.after(new Date())) {
			logger.error("Token " + token + " has expired on " + date);
			return null;
		}

		// extend expiration date
		document.field("expires", new DateTime().plusDays(30).toDate());
		document.save();

		return document.field("user", ORID.class).toString();
	}

	/**
	 * Token representer
	 */
	private class Token {
		private String selector;
		private String token;

		Token(String token) {
			if (token != null && !token.isEmpty()) {
				String[] parts = token.split(":");
				if (parts.length == 2) {
					this.selector = parts[0];
					String hashedToken;
					try {
						MessageDigest md = MessageDigest.getInstance("SHA-256");
						hashedToken = Hex.encodeHexString(md.digest(parts[1].getBytes("UTF-8")));
					} catch (Exception e) {
						logger.error("Could not SHA-256 algorith: keeping tokens in unsecure clear text format!");
						hashedToken = parts[1];
					}

					this.token = hashedToken;
				}
			}
		}

		boolean isValid() {
			return selector != null && token != null && !selector.isEmpty() && !token.isEmpty();
		}
	}
}
