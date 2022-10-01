package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrientDbConfigRepositoryTest {
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
	private OrientDbConfigRepository repository;

	@BeforeEach
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbConfigRepository.class);
	}

	@AfterEach
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("truncate class Config")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetValue() throws Exception {
		// add key
		factory.getDb().command(new OCommandSQL("INSERT INTO Config SET key = 'testKey1', value = 'testValue1'")).execute();

		// get non-existent key
		assertNull(repository.getValue("XYZ-NON-EXIST"));

		// get existing key
		assertEquals("testValue1", repository.getValue("testKey1"));
	}

	@Test
	public void testHasValue() throws Exception {
		// add key
		factory.getDb().command(new OCommandSQL("INSERT INTO Config SET key = 'testKey2', value = 'testValue2'")).execute();

		// get non-existent key
		assertFalse(repository.hasValue("XYZ-NON-EXIST"));

		// get existing key
		assertTrue(repository.hasValue("testKey2"));
	}

	@Test
	public void testSetValue() throws Exception {
		// add value
		repository.setValue("testKey3", "testValue3");

		// find config
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Config where key = ?");
		List<ODocument> result = factory.getDb().command(query).execute("testKey3");
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		ODocument config = result.get(0);

		assertTrue(config.field("value").equals("testValue3"));

		// add another value to test, if we really only create one entry
		repository.setValue("testKey3", "testValue3xy");

		// find config
		query = new OSQLSynchQuery<>("select * from Config where key = ?");
		result = factory.getDb().command(query).execute("testKey3");
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		config = result.get(0);

		// check updated value
		assertTrue(config.field("value").equals("testValue3xy"));
	}

	@Test
	public void testDeleteValue() throws Exception {
		// add key
		factory.getDb().command(new OCommandSQL("INSERT INTO Config SET key = 'testKey4', value = 'testValue4'")).execute();

		// delete value
		repository.deleteValue("testKey4");

		// find config
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Config where key = ?");
		List<ODocument> result = factory.getDb().command(query).execute("testKey4");
		assertTrue(result.isEmpty());
	}
}
