package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.UserGroup;
import org.segrada.model.prototype.IUserGroup;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OrientDbUserGroupRepositoryTest {
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
	private OrientDbUserGroupRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbUserGroupRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
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
		assertEquals("UserGroup", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		Map<String, String> roles = new HashMap<>();
		roles.put("Test", "1");
		roles.put("Test3", "-1");
		roles.put("Test5", "xxx"); // test robustness of code

		ODocument document = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", roles)
				.field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("special", "ADMIN").field("description", "description");

		IUserGroup userGroup = repository.convertToEntity(document);

		assertEquals("title", userGroup.getTitle());
		assertEquals("description", userGroup.getDescription());
		assertEquals(1, userGroup.getRole("Test"));
		assertEquals(0, userGroup.getRole("Test2"));
		assertEquals(-1, userGroup.getRole("Test3"));
		assertEquals(new Long(1L), userGroup.getCreated());
		assertEquals(new Long(2L), userGroup.getModified());
		assertEquals("ADMIN", userGroup.getSpecial());

		// check if groups are still the same after saving
		document.save();

		userGroup = repository.convertToEntity(document);

		assertEquals(1, userGroup.getRole("Test"));
		assertEquals(0, userGroup.getRole("Test2"));
		assertEquals(-1, userGroup.getRole("Test3"));
	}

	@Test
	public void testConvertToDocument() throws Exception {
		IUserGroup userGroup = new UserGroup();

		userGroup.setTitle("title");
		userGroup.setDescription("description");
		userGroup.setCreated(1L);
		userGroup.setModified(2L);
		userGroup.setSpecial("ADMIN");

		userGroup.setRole("Test");
		userGroup.setRole("Test3", -1);

		// first without id
		ODocument document = repository.convertToDocument(userGroup);

		assertEquals("title", document.field("title"));
		assertEquals("title", document.field("titleasc"));
		assertEquals("description", document.field("description"));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));
		assertEquals("ADMIN", document.field("special"));

		// check roles
		Map<String, String> roles = document.field("roles", OType.EMBEDDEDMAP);
		assertTrue(roles.containsKey("Test"));
		assertTrue(roles.containsKey("Test3"));

		assertEquals(1, roles.get("Test"));
		assertEquals(-1, roles.get("Test3"));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		userGroup.setId(id);

		ODocument newDocument = repository.convertToDocument(userGroup);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY titleasc", repository.getDefaultOrder(true));
		assertEquals(" titleasc", repository.getDefaultOrder(false));
	}

	@Test
	public void testFindSpecial() throws Exception {
		// sanity
		assertNull(repository.findSpecial(null));
		assertNull(repository.findSpecial(""));

		// no previously defined group?
		assertNull(repository.findSpecial("ADMIN"));

		Map<String, String> roles = new HashMap<>();
		roles.put("Test", "1");
		roles.put("Test3", "-1");
		roles.put("Test5", "xxx"); // test robustness of code

		ODocument document = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", roles)
				.field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true).field("special", "ADMIN");
		document.save();

		assertNotNull(repository.findSpecial("ADMIN"));
		assertEquals(document.getIdentity().toString(), repository.findSpecial("ADMIN").getId());
	}

	@Test
	public void testDelete() throws Exception {
		IUserGroup userGroup = new UserGroup();

		userGroup.setTitle("title");
		userGroup.setDescription("description");
		userGroup.setCreated(1L);
		userGroup.setModified(2L);

		userGroup.setRole("Test");
		userGroup.setRole("Test3", -1);

		// delete non-saved group?
		assertTrue(repository.delete(null));
		assertTrue(repository.delete(userGroup));

		// set special group
		userGroup.setSpecial("ADMIN");

		repository.save(userGroup);

		// disallow deletion
		assertFalse(repository.delete(userGroup));

		// set special group
		userGroup.setSpecial(null);

		repository.save(userGroup);

		// disallow deletion
		assertTrue(repository.delete(userGroup));
	}
}