package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import static org.junit.Assert.*;

public class AbstractOrientDbBaseRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * factory
	 */
	private OrientDbRepositoryFactory factory;

	@Before
	public void setUp() throws Exception {
		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// set factory
		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());
	}

	@Test
	public void testInitDb() throws Exception {
		// create correct repo
		MockOrientDbRepository mockOrientDbRepository = new MockOrientDbRepository(factory);

		if (!mockOrientDbRepository.db.isClosed()) mockOrientDbRepository.db.close();

		mockOrientDbRepository.initDb();

		assertFalse(mockOrientDbRepository.db.isClosed());

		// finally
		// close db
		try {
			mockOrientDbRepository.db.close();
		} catch (Exception e) {
			// do nothing
		}

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// close db
		db.close();
	}

	@Test
	public void testInitNullDb() throws Exception {
		// create instance with null
		MockOrientDbRepository mockOrientDbRepository = new MockOrientDbRepository(null);

		try {
			mockOrientDbRepository.initDb();
			fail("Exception expected");
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Partial/mock repository to test methods
	 */
	private class MockOrientDbRepository extends AbstractOrientDbBaseRepository {
		public MockOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
			super(repositoryFactory);
		}
	}
}