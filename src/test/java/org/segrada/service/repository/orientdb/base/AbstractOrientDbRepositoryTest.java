package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.session.ApplicationSettings;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.*;

public class AbstractOrientDbRepositoryTest {
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
		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// create schema
		db.command(new OCommandSQL("create class Mock")).execute();

		// close db
		db.close();

		// create repo
		mockOrientDbRepository = new MockOrientDbRepository(orientDBTestInstance.getDatabase(), new OrientDbTestApplicationSettings());
	}

	@After
	public void tearDown() throws Exception {
		// close db
		try {
			mockOrientDbRepository.db.close();
		} catch (Exception e) {
			// do nothing
		}

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// remove schema
		db.command(new OCommandSQL("drop class Mock")).execute();

		// close db
		db.close();
	}

	@Test
	public void testInitDb() throws Exception {
		if (!mockOrientDbRepository.db.isClosed()) mockOrientDbRepository.db.close();

		mockOrientDbRepository.initDb();

		assertFalse(mockOrientDbRepository.db.isClosed());

		// force exception
		//TODO: with final, it does not work anymore - think of another exception to produce...
		/*mockOrientDbRepository.db = null;
		try {
			mockOrientDbRepository.initDb();
			fail("Exception expected");
		} catch (Exception e) {
			// do nothing
		}*/
	}

	@Test
	public void testCreateOrLoadDocument() throws Exception {
		// create new entity
		MockEntity mockEntity1 = new MockEntity();
		ODocument document1 = mockOrientDbRepository.createOrLoadDocument(mockEntity1);

		assertNotNull(document1);
		assertEquals("#-1:-1", document1.getIdentity().toString());

		// save entity to create an id
		document1.save();
		String id = document1.getIdentity().toString();

		MockEntity mockEntity2 = new MockEntity();
		mockEntity2.setId(id);

		ODocument document2 = mockOrientDbRepository.createOrLoadDocument(mockEntity2);
		assertEquals(id, document1.getIdentity().toString());

		// create non existent entity and test create/load
		id = document2.getIdentity().getClusterId() + ":999999";
		MockEntity mockEntity3 = new MockEntity();
		mockEntity3.setId(id);

		ODocument document3 = mockOrientDbRepository.createOrLoadDocument(mockEntity3);
		assertNull(document3);

		// create entity with dummy cluster
		MockEntity mockEntity4 = new MockEntity();
		mockEntity4.setId("#999:0");

		try {
			mockOrientDbRepository.createOrLoadDocument(mockEntity4);
			fail("Exception expected");
		} catch (Exception e) {
			// that is expected!
		}

		// get id of wrong class - create a schema
		mockOrientDbRepository.db.command(new OCommandSQL("create class MockTest")).execute();

		// create document of MockTest class
		ODocument mockMock = new ODocument("MockTest").save();

		// create entity with id of wrong class
		MockEntity mockEntity5 = new MockEntity();
		mockEntity5.setId(mockMock.getIdentity().toString());

		try { // should create a runtime exception
			mockOrientDbRepository.createOrLoadDocument(mockEntity5);
			fail("Exception expected");
		} catch (RuntimeException e) {
			// that is expected!
		}

		mockOrientDbRepository.db.command(new OCommandSQL("drop class MockTest")).execute();
	}

	@Test
	public void testProcessBeforeSaving() throws Exception {
		// create new entity
		MockEntity mockEntity1 = new MockEntity();
		MockEntity mockEntity2 = mockOrientDbRepository.processBeforeSaving(mockEntity1);

		assertEquals(mockEntity1, mockEntity2);

	}

	@Test
	public void testProcessAfterSaving() throws Exception {
		MockEntity mockEntity1 = new MockEntity();

		// should be null in the beginning
		assertNull(mockEntity1.getId());

		// create and save document
		ODocument document = new ODocument("Mock").save();

		// run after saving
		MockEntity mockEntity2 = mockOrientDbRepository.processAfterSaving(document, mockEntity1);

		assertEquals(mockEntity1, mockEntity2);
		assertEquals(document.getIdentity().toString(), mockEntity1.getId());
		assertEquals(document.getIdentity().toString(), mockEntity2.getId());
	}

	@Test
	public void testSave() throws Exception {
		// add one entity
		MockEntity entity = new MockEntity();

		// no id at beginning
		assertEquals(null, entity.getId());

		mockOrientDbRepository.save(entity);

		// id should have been defined
		assertNotNull(entity.getId());
		assertTrue(entity.getVersion() > 0);

		// check if crecord is in database
		assertNotNull(mockOrientDbRepository.db.load(new ORecordId(entity.getId())));
	}

	@Test
	public void testCount() throws Exception {
		// should be empty at the start
		assertEquals(0L, mockOrientDbRepository.count());

		// add one entity
		MockEntity entity = new MockEntity();
		mockOrientDbRepository.save(entity);

		// should contain one entity
		assertEquals(1L, mockOrientDbRepository.count());

		// add another entity
		MockEntity entity2 = new MockEntity();
		mockOrientDbRepository.save(entity2);

		// should contain two entities
		assertEquals(2L, mockOrientDbRepository.count());

		// remove entity
		mockOrientDbRepository.delete(entity2);

		// should contain one entity
		assertEquals(1L, mockOrientDbRepository.count());

	}

	@Test
	public void testFindAll() throws Exception {
		// should return an empty list
		List<MockEntity> emptyList = mockOrientDbRepository.findAll();

		assertEquals(0, emptyList.size());

		// add two entities
		MockEntity entity1 = new MockEntity();
		mockOrientDbRepository.save(entity1);
		MockEntity entity2 = new MockEntity();
		mockOrientDbRepository.save(entity2);

		// should return an empty list
		List<MockEntity> filledList = mockOrientDbRepository.findAll();

		// correct size of list?
		assertEquals(2, filledList.size());

		// ids should be the same
		assertEquals(entity1.getId(), filledList.get(0).getId());
		assertEquals(entity2.getId(), filledList.get(1).getId());
	}

	@Test
	public void testFind() throws Exception {
		// create and save document
		ODocument document = new ODocument("Mock").save();

		// add one entity
		MockEntity entity = new MockEntity();
		entity.setId(document.getIdentity().toString());
		entity.setVersion(document.getVersion());

		MockEntity found = mockOrientDbRepository.find(document.getIdentity().toString());

		// should be the same id
		assertEquals(entity.getId(), found.getId());

		// find unsaved entity should not throw error
		assertNull(mockOrientDbRepository.find(null));
	}

	@Test
	public void testDelete() throws Exception {
		// create and save document
		ODocument document = new ODocument("Mock").save();

		// add one entity
		MockEntity entity = new MockEntity();
		entity.setId(document.getIdentity().toString());

		// now delete entity
		mockOrientDbRepository.delete(entity);

		// should not be found any more
		assertNull(mockOrientDbRepository.db.load(document.getRecord()));

	}

	@Test
	public void testGetDefaultQueryParameters() throws Exception {
		assertEquals("", mockOrientDbRepository.getDefaultQueryParameters());

		assertEquals("", mockOrientDbRepository.getDefaultQueryParameters(true));
		assertEquals("", mockOrientDbRepository.getDefaultQueryParameters(false));
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals("", mockOrientDbRepository.getDefaultOrder());

		assertEquals("", mockOrientDbRepository.getDefaultOrder(true));
		assertEquals("", mockOrientDbRepository.getDefaultOrder(false));
	}

	/**
	 * Mock entity
	 */
	private class MockEntity extends AbstractSegradaEntity implements SegradaEntity {
		@Override
		public boolean equals(Object that) {
			return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public String getTitle() {
			return "DUMMY";
		}
	}

	/**
	 * Partial/mock repository to test methods
	 */
	private class MockOrientDbRepository extends AbstractOrientDbRepository<MockEntity> {
		/**
		 * @param db                  database instance
		 * @param applicationSettings application settings instance
		 */
		public MockOrientDbRepository(ODatabaseDocumentTx db, ApplicationSettings applicationSettings) {
			super(db, applicationSettings);
		}

		@Override
		public MockEntity convertToEntity(ODocument document) {
			MockEntity entity = new MockEntity();
			entity.setId(document.getIdentity().toString());
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
}