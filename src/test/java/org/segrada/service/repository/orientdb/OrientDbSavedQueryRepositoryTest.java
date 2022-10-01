package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.segrada.model.SavedQuery;
import org.segrada.model.User;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import static org.junit.jupiter.api.Assertions.*;

public class OrientDbSavedQueryRepositoryTest {
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
	private OrientDbSavedQueryRepository repository;

	@BeforeEach
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbSavedQueryRepository.class);
	}

	@AfterEach
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("truncate class SavedQuery")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("SavedQuery", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument document = new ODocument("SavedQuery").field("title", "title").field("titleasc", "title")
				.field("type", "type").field("description", "description").field("created", 1L).field("modified", 2L)
				.field("data", "data");
		// persist to database to create id
		document.save();

		ISavedQuery savedQuery = repository.convertToEntity(document);

		assertEquals("type", savedQuery.getType());
		assertEquals("title", savedQuery.getTitle());
		assertEquals("description", savedQuery.getDescription());
		assertEquals(new Long(1L), savedQuery.getCreated());
		assertEquals(new Long(2L), savedQuery.getModified());
		assertEquals("data", savedQuery.getData());
		assertEquals(document.getIdentity().toString(), savedQuery.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		ISavedQuery savedQuery = new SavedQuery();
		savedQuery.setType("type");
		savedQuery.setTitle("title");
		savedQuery.setDescription("description");
		savedQuery.setCreated(1L);
		savedQuery.setModified(2L);
		savedQuery.setData("data");

		// first without id
		ODocument document = repository.convertToDocument(savedQuery);

		assertEquals("type", document.field("type"));
		assertEquals("title", document.field("title"));
		assertEquals("description", document.field("description"));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));
		assertEquals("data", document.field("data"));

		// class name should be correct
		assertEquals("SavedQuery", document.getClassName());

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		savedQuery.setId(id);

		ODocument newDocument = repository.convertToDocument(savedQuery);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY titleasc", repository.getDefaultOrder(true));
		assertEquals(" titleasc", repository.getDefaultOrder(false));
	}

	@Test
	public void testPaginate() throws Exception {
		//fail("Test not implemented yet.");
		//TODO: do later
	}

	@Test
	public void testFindAllBy() throws Exception {
		// test empty repository
		assertEquals(0, repository.findAllBy(null, null, null).size());
		assertEquals(0, repository.findAllBy(new User(), null, null).size());
		assertEquals(0, repository.findAllBy(new User(), null, "test").size());
		assertEquals(0, repository.findAllBy(new User(), "test", null).size());
		assertEquals(0, repository.findAllBy(null, "test", "test").size());
		assertEquals(0, repository.findAllBy(new User(), "test", "test").size());

		// fill repository
		for (int i = 0; i < 10; i++) {
			ISavedQuery savedQuery = new SavedQuery();
			savedQuery.setTitle("Query " + i);
			savedQuery.setType(i % 2 == 0 ? "graph" : "dummy");

			repository.save(savedQuery);
		}

		// test queries
		assertEquals(10, repository.findAllBy(null, null, null).size());
		assertEquals(5, repository.findAllBy(null, "graph", null).size());
		assertEquals(10, repository.findAllBy(null, null, "query").size());
		assertEquals(1, repository.findAllBy(null, null, "query 2").size());
		assertEquals(1, repository.findAllBy(null, "graph", "query 2").size());
		assertEquals(1, repository.findAllBy(null, "dummy", "query 1").size());
		assertEquals(0, repository.findAllBy(null, "dummy", "query 2").size());
	}
}
