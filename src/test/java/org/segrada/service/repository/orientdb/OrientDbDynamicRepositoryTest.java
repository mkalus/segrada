package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.orientdb.base.AbstractOrientDbRepository;
import org.segrada.session.ApplicationSettings;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OrientDbDynamicRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * refrence to mock repository - see below
	 */
	private MockOrientDbRepository mockOrientDbRepository;

	/**
	 * mock repository - see below
	 */
	private OrientDbDynamicRepository repository;

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

		// create map of repository
		Map<String, AbstractOrientDbRepository> repositoryMap = new HashMap<>();
		repositoryMap.put("Mock", mockOrientDbRepository);

		// create repository
		repository = new OrientDbDynamicRepository(repositoryMap);
	}

	@After
	public void tearDown() throws Exception {
		// close db
		try {
			mockOrientDbRepository.getDb().close();
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
	public void testConvertToEntity() throws Exception {
		ODocument document = new ODocument("Mock");

		// first without id
		SegradaEntity testEntity = repository.convertToEntity(document);
		assertNotNull(testEntity);
		assertEquals("#-1:-1", testEntity.getId());
		assertTrue(testEntity instanceof MockEntity);

		// now save and convert
		document.save();
		testEntity = repository.convertToEntity(document);
		assertNotNull(testEntity);
		assertEquals(document.getIdentity().toString(), testEntity.getId());
		assertTrue(testEntity instanceof MockEntity);
	}

	@Test
	public void testFind() throws Exception {
		// add one entity
		MockEntity entity = new MockEntity();
		mockOrientDbRepository.save(entity);

		SegradaEntity testEntity = repository.find(entity.getId(), "Mock");
		assertNotNull(testEntity);
		assertEquals(entity.getId(), testEntity.getId());
		assertTrue(testEntity instanceof MockEntity);
	}

	@Test
	public void testGetRepository() throws Exception {
		assertEquals(mockOrientDbRepository, repository.getRepository("Mock"));
		assertNull(repository.getRepository("Dummy"));
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