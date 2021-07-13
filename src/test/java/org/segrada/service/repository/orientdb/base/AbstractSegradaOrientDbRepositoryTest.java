package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Node;
import org.segrada.model.User;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.*;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractSegradaOrientDbRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * mock repository - see below
	 */
	private MockOrientDbRepository mockOrientDbRepository;

	@Before
	public void setUp() throws Exception {
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// create schema
		db.command(new OCommandSQL("create class Mock")).execute();
		db.command(new OCommandSQL("create class MockUser")).execute();

		OrientDbRepositoryFactory factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repo
		mockOrientDbRepository = new MockOrientDbRepository(factory);
	}

	@After
	public void tearDown() throws Exception {
		// close db
		try {
			if (mockOrientDbRepository.db != null)
				mockOrientDbRepository.db.close();
		} catch (Exception e) {
			// do nothing
		}

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// remove schema
		db.command(new OCommandSQL("drop class Mock")).execute();
		db.command(new OCommandSQL("drop class MockUser")).execute();
		db.command(new OCommandSQL("truncate class User")).execute();
		db.command(new OCommandSQL("truncate class UserGroup")).execute();
		db.command(new OCommandSQL("delete vertex V")).execute();
		db.command(new OCommandSQL("delete edge E")).execute();

		// close db
		db.close();
	}

	@Test
	public void testPopulateODocumentWithCreatedModified() throws Exception {
		// create new entity
		MockEntity mockEntity1 = new MockEntity();
		ODocument document1 = new ODocument("Mock");

		// test empty population of data
		mockOrientDbRepository.populateODocumentWithCreatedModified(document1, mockEntity1);

		// is null
		assertEquals(new Long(0L), document1.field("created"));
		assertEquals(new Long(0L), document1.field("modified"));
		assertNull(document1.field("creator"));
		assertNull(document1.field("modifier"));

		// ok, now we add some data
		ODocument oCreator = new ODocument("MockUser");
		oCreator.save();
		ODocument oModifier = new ODocument("MockUser");
		oModifier.save();

		IUser creator = new User();
		creator.setId(oCreator.getIdentity().toString());
		IUser modifier = new User();
		modifier.setId(oModifier.getIdentity().toString());

		// create new entity
		MockEntity mockEntity2 = new MockEntity();
		mockEntity2.setCreated(1L);
		mockEntity2.setModified(2L);
		mockEntity2.setCreator(creator);
		mockEntity2.setModifier(modifier);
		ODocument document2 = new ODocument("Mock");

		// test empty population of data
		mockOrientDbRepository.populateODocumentWithCreatedModified(document2, mockEntity2);

		assertEquals(new Long(1L), document2.field("created"));
		assertEquals(new Long(2L), document2.field("modified"));
		assertEquals(new ORecordId(creator.getId()), document2.field("creator", ORecordId.class));
		assertEquals(new ORecordId(modifier.getId()), document2.field("modifier", ORecordId.class));
	}

	@Test
	public void testPopulateEntityWithCreatedModified() throws Exception {
		// create two users
		long now = System.currentTimeMillis();
		ODocument oCreator = new ODocument("MockUser");
		oCreator.save();
		ODocument oModifier = new ODocument("MockUser");
		oModifier.save();

		// first test without creator/modifier
		ODocument document1 = new ODocument("Mock");
		document1.field("created", 1L).field("modified", 2L);
		MockEntity entity1 = new MockEntity();

		mockOrientDbRepository.populateEntityWithCreatedModified(document1, entity1);

		assertEquals(new Long(1L), entity1.getCreated());
		assertEquals(new Long(2L), entity1.getModified());
		assertNull(entity1.getCreator());
		assertNull(entity1.getModifier());


		// now test with creator/modifier
		ODocument document2 = new ODocument("Mock");
		document2.field("created", 1L).field("modified", 2L)
				.field("creator", oCreator)
				.field("modifier", oModifier);
		MockEntity entity2 = new MockEntity();

		mockOrientDbRepository.populateEntityWithCreatedModified(document2, entity2);

		assertEquals(new Long(1L), entity2.getCreated());
		assertEquals(new Long(2L), entity2.getModified());
		assertNotNull(entity2.getCreator());
		assertNotNull(entity2.getModifier());
		assertEquals(oCreator.getIdentity().toString(), entity2.getCreator().getId());
		assertEquals(oModifier.getIdentity().toString(), entity2.getModifier().getId());
	}

	@Test
	public void testProcessBeforeSaving() throws Exception {
		// create new entity
		MockEntity mockEntity1 = new MockEntity();

		// process without any identity data
		MockEntity testEntity1 = mockOrientDbRepository.processBeforeSaving(mockEntity1);

		// should still be the same
		assertEquals(mockEntity1, testEntity1);
		assertNotNull(mockEntity1.getCreated());
		assertNotNull(mockEntity1.getModified());
		assertNull(mockEntity1.getCreator());
		assertNull(mockEntity1.getModifier());

		assertEquals(mockEntity1.getCreated(), testEntity1.getCreated());
		assertEquals(mockEntity1.getModified(), testEntity1.getModified());
		assertEquals(mockEntity1.getCreator(), testEntity1.getCreator());
		assertEquals(mockEntity1.getModifier(), testEntity1.getModifier());

		// process again to test modified and created
		mockEntity1.setId("#99:99"); // simulate save
		Long created = mockEntity1.getCreated();
		Long modified = mockEntity1.getModified();

		// wait to force change of modification time
		Thread.sleep(5L);

		MockEntity testEntity1b = mockOrientDbRepository.processBeforeSaving(mockEntity1);

		assertEquals(created, mockEntity1.getCreated());
		assertEquals(created, testEntity1b.getCreated());
		assertNotEquals(modified, mockEntity1.getModified());
		assertNotEquals(modified, testEntity1b.getModified());



		// now test with identity set
		// ok, now we add some data
		IUser creator = new User();
		creator.setId("#99:0");
		IUser modifier = new User();
		modifier.setId("#99:1");

		// set identity to creator
		mockOrientDbRepository.repositoryFactory.getIdentity().setUser(creator);

		// create new entity
		MockEntity mockEntity2 = new MockEntity();

		// process without any identity data
		MockEntity testEntity2 = mockOrientDbRepository.processBeforeSaving(mockEntity2);

		// should still be the same
		assertEquals(mockEntity2, testEntity2);
		assertNotNull(mockEntity2.getCreated());
		assertNotNull(mockEntity2.getModified());
		assertNotNull(mockEntity2.getCreator());
		assertNotNull(mockEntity2.getModifier());
		assertEquals(creator, mockEntity2.getCreator());
		assertEquals(creator, mockEntity2.getModifier());
		assertEquals(creator, testEntity2.getCreator());
		assertEquals(creator, testEntity2.getModifier());

		assertEquals(mockEntity2.getCreated(), testEntity2.getCreated());
		assertEquals(mockEntity2.getModified(), testEntity2.getModified());
		assertEquals(mockEntity2.getCreator(), testEntity2.getCreator());
		assertEquals(mockEntity2.getModifier(), testEntity2.getModifier());

		// process again to test modified and created
		mockEntity2.setId("#99:99"); // simulate save
		created = mockEntity2.getCreated();
		modified = mockEntity2.getModified();
		mockOrientDbRepository.repositoryFactory.getIdentity().setUser(modifier); // new user

		// wait to force change of modification time
		Thread.sleep(5L);

		MockEntity testEntity2b = mockOrientDbRepository.processBeforeSaving(mockEntity2);

		assertEquals(created, mockEntity2.getCreated());
		assertEquals(created, testEntity2b.getCreated());
		assertNotEquals(modified, mockEntity2.getModified());
		assertNotEquals(modified, testEntity2b.getModified());

		// modifier should have been set correctly
		assertEquals(creator, mockEntity2.getCreator());
		assertEquals(modifier, mockEntity2.getModifier());
		assertEquals(creator, testEntity2b.getCreator());
		assertEquals(modifier, testEntity2b.getModifier());
	}

	@Test
	public void testConvertToUser() throws Exception {
		ODocument group = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", new HashMap<String, String>())
				.field("created", 1L).field("modified", 2L)
				.field("active", true);
		group.save();

		ODocument document = new ODocument("User").field("login", "login")
				.field("password", "password").field("name", "name")
				.field("group", group).field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true);

		IUser user = mockOrientDbRepository.convertToUser(document);

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
	public void testConvertToUserGroup() throws Exception {
		Map<String, Integer> roles = new HashMap<>();
		roles.put("Test", 1);
		roles.put("Test3", -1);

		ODocument document = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", roles)
				.field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("special", "ADMIN").field("description", "description");

		IUserGroup userGroup = mockOrientDbRepository.convertToUserGroup(document);

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

		userGroup = mockOrientDbRepository.convertToUserGroup(document);

		assertEquals(1, userGroup.getRole("Test"));
		assertEquals(0, userGroup.getRole("Test2"));
		assertEquals(-1, userGroup.getRole("Test3"));
	}

	@Test
	public void testConvertToPictogram() throws Exception {
		ODocument document = new ODocument("Pictogram").field("title", "title").field("titleasc", "title")
				.field("fileIdentifier", "test.txt")
				.field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		IPictogram pictogram = mockOrientDbRepository.convertToPictogram(document);

		assertEquals("title", pictogram.getTitle());
		assertEquals("test.txt", pictogram.getFileIdentifier());
		assertEquals(new Long(1L), pictogram.getCreated());
		assertEquals(new Long(2L), pictogram.getModified());
		assertEquals(document.getIdentity().toString(), pictogram.getId());
	}

	@Test
	public void testLazyLoadUser() throws Exception {
		ODocument group = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", new HashMap<String, String>())
				.field("created", 1L).field("modified", 2L)
				.field("active", true);
		group.save();

		ODocument document = new ODocument("User").field("login", "login")
				.field("password", "password").field("name", "name").field("nameasc", "name")
				.field("created", 1L).field("modified", 2L).field("group", group)
				.field("lastLogin", 3L).field("active", true);
		document.save();

		IUser user = mockOrientDbRepository.lazyLoadUser(new ORecordId(document.getIdentity()));
		assertNotNull(user);
		assertEquals(document.getIdentity().toString(), user.getId());
	}

	@Test
	public void testLazyLoadUserGroup() throws Exception {
		Map<String, Integer> roles = new HashMap<>();
		roles.put("Test", 1);
		roles.put("Test3", -1);

		ODocument document = new ODocument("UserGroup").field("title", "title")
				.field("titleasc", "titleasc").field("roles", roles)
				.field("created", 1L).field("modified", 2L)
				.field("lastLogin", 3L).field("active", true);
		document.save();

		IUserGroup userGroup = mockOrientDbRepository.lazyLoadUserGroup(new ORecordId(document.getIdentity()));

		assertNotNull(userGroup);
		assertEquals(document.getIdentity().toString(), userGroup.getId());
	}

	/**
	 * Mock entity
	 */
	private class MockEntity extends AbstractSegradaEntity {
		private String id;

		@Override
		public void setId(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getTitle() {
			return "DUMMY";
		}
	}

	/**
	 * Partial/mock repository to test methods
	 */
	private class MockOrientDbRepository extends AbstractSegradaOrientDbRepository<MockEntity> {
		public MockOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
			super(repositoryFactory);
		}

		@Override
		public MockEntity convertToEntity(ODocument document) {
			MockEntity entity = new MockEntity();
			populateEntityWithBaseData(document, entity);
			return entity;
		}

		@Override
		public ODocument convertToDocument(MockEntity entity) {
			ODocument document = createOrLoadDocument(entity);
			return document;
		}

		@Override
		public String getModelClassName() {
			return "Mock";
		}
	}

	//updateEntityTags tested in AbstractAnnotatedOrientDbRepositoryTest
}