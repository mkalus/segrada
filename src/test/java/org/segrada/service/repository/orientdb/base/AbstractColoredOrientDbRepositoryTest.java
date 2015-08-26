package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.base.AbstractColoredModel;
import org.segrada.model.prototype.IPictogram;
import org.segrada.model.prototype.SegradaColoredEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AbstractColoredOrientDbRepositoryTest {
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
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// create schema
		db.command(new OCommandSQL("create class Mock extends V")).execute();

		OrientDbRepositoryFactory factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repo
		mockOrientDbRepository = new MockOrientDbRepository(factory);
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
		db.command(new OCommandSQL("delete vertex V")).execute();
		db.command(new OCommandSQL("delete edge E")).execute();
		db.command(new OCommandSQL("truncate class Pictogram")).execute();
		db.command(new OCommandSQL("drop class Mock")).execute();

		// close db
		db.close();
	}

	@Test
	public void testPopulateODocumentWithColored() throws Exception {
		// create new entity
		MockEntity mockEntity1 = new MockEntity();
		ODocument document1 = new ODocument("Mock");

		// test empty population of data
		mockOrientDbRepository.populateEntityWithColored(document1, mockEntity1);

		// should exist...
		assertNull(document1.field("color"));
		assertNull(document1.field("pictogram"));

		// ok, now we add some data
		ODocument document = new ODocument("Pictogram").field("title", "title").field("titleasc", "title")
				.field("fileIdentifier", "test.txt")
				.field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		IPictogram pictogram = mockOrientDbRepository.convertToPictogram(document);

		// create new entity
		MockEntity mockEntity2 = new MockEntity();
		mockEntity2.setPictogram(pictogram);
		mockEntity2.setColor(123456);
		ODocument document2 = new ODocument("Mock");

		// test empty population of data
		mockOrientDbRepository.populateODocumentWithColored(document2, mockEntity2);

		assertEquals(new ORecordId(pictogram.getId()), document2.field("pictogram", ORecordId.class));
		assertEquals(new Integer(123456), document2.field("color"));
	}

	@Test
	public void testPopulateEntityWithColored() throws Exception {
		ODocument pictogram = new ODocument("Pictogram").field("title", "title").field("titleasc", "title").field("titleasc", "title").field("fileIdentifier", "test.txt")
				.field("created", 1L).field("modified", 2L);
		pictogram.save();

		// first test without data
		ODocument document1 = new ODocument("Mock");

		MockEntity entity1 = new MockEntity();

		mockOrientDbRepository.populateEntityWithColored(document1, entity1);

		assertNull(entity1.getPictogram());
		assertNull(entity1.getColor());

		// now test with data
		ODocument document2 = new ODocument("Mock");
		document2.field("pictogram", pictogram);
		document2.field("color", 123456);

		MockEntity entity2 = new MockEntity();

		mockOrientDbRepository.populateEntityWithColored(document2, entity2);

		assertEquals(pictogram.getIdentity().toString(), entity2.getPictogram().getId());
		assertEquals(new Integer(123456), entity2.getColor());
	}

	/**
	 * Mock entity
	 */
	private class MockEntity extends AbstractColoredModel implements SegradaColoredEntity {
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
	private class MockOrientDbRepository extends AbstractColoredOrientDbRepository<MockEntity> {
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
			return createOrLoadDocument(entity);
		}

		@Override
		public String getModelClassName() {
			return "Mock";
		}
	}
}