package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.IUserGroup;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.HashMap;

import static org.junit.Assert.*;

public class OrientDbUserRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * reference to factory
	 */
	private OrientDbRepositoryFactory factory;

	/**
	 * repository to test
	 */
	private OrientDbUserRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbUserRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("truncate class User")).execute();
		factory.getDb().command(new OCommandSQL("truncate class UserGroup")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("User", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument group = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", new HashMap<String, String>())
				.field("created", 1L).field("modified", 2L)
				.field("active", true);
		group.save();

		ODocument document = new ODocument("User").field("login", "login")
				.field("password", "password").field("name", "name")
				.field("group", group).field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true);

		IUser user = repository.convertToEntity(document);

		assertEquals("login", user.getLogin());
		assertEquals("password", user.getPassword());
		assertEquals("name", user.getName());
		assertEquals(group.getIdentity().toString(), user.getGroup().getId());
		assertEquals(new Long(1L), user.getCreated());
		assertEquals(new Long(2L), user.getModified());
		assertEquals(new Long(3L), user.getLastLogin());
		assertEquals(true, user.getActive());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		ODocument group = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", new HashMap<String, String>())
				.field("created", 1L).field("modified", 2L)
				.field("active", true);
		group.save();
		IUserGroup userGroup = repository.convertToUserGroup(group);

		IUser user = new User();
		user.setLogin("login");
		user.setPassword("password");
		user.setName("name");
		user.setGroup(userGroup);
		user.setCreated(1L);
		user.setModified(2L);
		user.setLastLogin(3L);
		user.setActive(true);

		// first without id
		ODocument document = repository.convertToDocument(user);

		assertEquals("login", document.field("login"));
		assertEquals("password", document.field("password"));
		assertEquals("name", document.field("name"));
		assertEquals(userGroup.getId(), document.field("group", String.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));
		assertEquals(new Long(3L), document.field("lastLogin", Long.class));
		assertEquals(true, document.field("active"));
		// empty id in orient
		assertEquals("#-1:-1", document.getIdentity().toString());

		// class name should be correct
		assertEquals("User", document.getClassName());

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		user.setId(id);

		ODocument newDocument = repository.convertToDocument(user);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindByLogin() throws Exception {
		ODocument group = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", new HashMap<String, String>())
				.field("created", 1L).field("modified", 2L)
				.field("active", true);
		group.save();
		IUserGroup userGroup = repository.convertToUserGroup(group);

		IUser user = new User();
		user.setLogin("login");
		user.setPassword("password");
		user.setName("name");
		user.setGroup(userGroup);
		user.setCreated(1L);
		user.setModified(2L);
		user.setLastLogin(3L);
		user.setActive(true);

		repository.save(user);

		IUser testUser = repository.findByLogin(user.getLogin());

		assertEquals(user.getId(), testUser.getId());

		// test not finding user
		testUser = repository.findByLogin("NOT-existing");
		assertNull(testUser);
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY name", repository.getDefaultOrder(true));
		assertEquals(" name", repository.getDefaultOrder(false));
	}

	@Test
	public void testPaginate() throws Exception {
		//fail("Test not implemented yet.");
		//TODO: do later
	}
}