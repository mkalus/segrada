package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.test.OrientDBTestInstance;

import java.security.MessageDigest;
import java.util.List;

import static org.junit.Assert.*;

public class OrientRememberMeRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * reference to db
	 */
	ODatabaseDocumentTx db;

	/**
	 * reference to instance
	 */
	private OrientRememberMeRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		db = orientDBTestInstance.getDatabase();

		// create instance
		repository = new OrientRememberMeRepository(db);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		db.command(new OCommandSQL("truncate class User")).execute();
		db.command(new OCommandSQL("truncate class RememberMeToken")).execute();

		// close db
		try {
			db.close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testCreateTokenForCookie() throws Exception {
		// create new user
		ODocument document = new ODocument("User").field("login", "login")
				.field("password", "password").field("name", "name").field("nameasc", "name")
				.field("role", "USER").field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true);
		document.save();

		String token = repository.createTokenForCookie(document.getIdentity().toString());

		assertNotNull(token);

		String[] parts = token.split(":");
		assertTrue(parts.length == 2);

		// good for now - check if saved to database
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from RememberMeToken where selector = ?");
		List<ODocument> result = db.command(query).execute(parts[0]);

		assertTrue(result.size() == 1);
		ODocument testDoc = result.get(0);

		assertEquals(document.getIdentity().toString(), testDoc.field("user", ORID.class).toString());

		// compare tokens
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String testToken = Hex.encodeHexString(md.digest(parts[1].getBytes("UTF-8")));

		assertEquals(testToken, testDoc.field("token", String.class));

		// create another token for the same user - should be a different token
		testToken = repository.createTokenForCookie(document.getIdentity().toString());

		assertNotEquals(token, testToken);
	}

	@Test
	public void testRemoveToken() throws Exception {
		// create new user
		ODocument document = new ODocument("User").field("login", "login")
				.field("password", "password").field("name", "name").field("nameasc", "name")
				.field("role", "USER").field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true);
		document.save();

		String token = repository.createTokenForCookie(document.getIdentity().toString());

		assertNotNull(token);

		// robustness checks
		String[] parts = token.split(":");
		assertTrue(parts.length == 2);

		assertFalse(repository.removeToken(parts[0].concat(":").concat("token")));
		assertFalse(repository.removeToken("anything".concat(":").concat(parts[1])));
		assertFalse(repository.removeToken(null));
		assertFalse(repository.removeToken(""));

		// now remove token by token name
		assertTrue(repository.removeToken(token));

		// get token from db
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from RememberMeToken where selector = ?");
		List<ODocument> result = db.command(query).execute(parts[0]);

		// should not be in database any more
		assertTrue(result.size() == 0);

		// remove again to make sure there is no error
		assertFalse(repository.removeToken(token));
	}

	@Test
	public void testValidateTokenAndGetUserId() throws Exception {
		// create new user
		ODocument document = new ODocument("User").field("login", "login")
				.field("password", "password").field("name", "name").field("nameasc", "name")
				.field("role", "USER").field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true);
		document.save();

		String token = repository.createTokenForCookie(document.getIdentity().toString());

		assertNotNull(token);

		// validate token
		String userId = repository.validateTokenAndGetUserId(token);

		assertEquals(document.getIdentity().toString(), userId);

		// try validate invalid token
		String[] parts = token.split(":");
		assertNull(repository.validateTokenAndGetUserId(parts[0].concat(":").concat("token")));

		// robustness checks
		assertNull(repository.validateTokenAndGetUserId("anything".concat(":").concat(parts[1])));
		assertNull(repository.validateTokenAndGetUserId(null));
		assertNull(repository.validateTokenAndGetUserId(""));

		// now remove token by token name
		assertTrue(repository.removeToken(token));

		// validate non existing token
		userId = repository.validateTokenAndGetUserId(token);

		assertNull(userId);
	}
}