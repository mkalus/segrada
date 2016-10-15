package org.segrada.service.base;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Color;
import org.segrada.model.prototype.IColor;
import org.segrada.service.repository.ColorRepository;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractRepositoryServiceTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * reference to factory
	 */
	private OrientDbRepositoryFactory factory;

	/**
	 * service to test
	 */
	private MockService service;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create service to test
		service = new MockService(factory);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("truncate class Color")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testFindById() throws Exception {
		ODocument document = new ODocument("Color").field("title", "title").field("titleasc", "title")
				.field("color", 123456).field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		IColor color = service.findById(document.getIdentity().toString());

		assertEquals(document.getIdentity().toString(), color.getId());
	}

	@Test
	public void testSave() throws Exception {
		IColor color = new Color();
		color.setTitle("title");
		color.setColor(123456);

		assertTrue(service.save(color));

		// check existence
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from " + color.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertFalse(result.isEmpty());
	}

	@Test
	public void testDelete() throws Exception {
		IColor color = new Color();
		color.setTitle("title");
		color.setColor(123456);

		service.save(color);

		// delete it
		service.delete(color);

		// check existence
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from " + color.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());
	}

	@Test
	public void testFindAll() throws Exception {
		assertFalse(service.findAll().iterator().hasNext());

		IColor color = new Color();
		color.setTitle("title");
		color.setColor(123456);

		service.save(color);

		Iterator it = service.findAll().iterator();
		assertTrue(it.hasNext());

		it.next();
		assertFalse(it.hasNext());

		// delete it
		service.delete(color);

		assertFalse(service.findAll().iterator().hasNext());
	}

	@Test
	public void testCount() throws Exception {
		assertEquals(0, service.count());

		IColor color = new Color();
		color.setTitle("title");
		color.setColor(123456);

		service.save(color);

		assertEquals(1, service.count());

		// delete it
		service.delete(color);

		assertEquals(0, service.count());
	}

	/**
	 * mock service class based on color repository - should be a mock repository, but I am too lazy now
	 */
	private class MockService extends AbstractRepositoryService<IColor, ColorRepository> {
		/**
		 * Constructor
		 */
		@Inject
		public MockService(RepositoryFactory repositoryFactory) {
			super(repositoryFactory, ColorRepository.class);
		}

		@Override
		public IColor createNewInstance() {
			return new Color();
		}

		@Override
		public Class<IColor> getModelClass() {
			return IColor.class;
		}
	}
}