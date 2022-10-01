package org.segrada.service.repository.orientdb.factory;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.segrada.service.repository.CommentRepository;
import org.segrada.service.repository.FileRepository;
import org.segrada.service.repository.orientdb.OrientDbCommentRepository;
import org.segrada.service.repository.orientdb.OrientDbFileRepository;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import static org.junit.jupiter.api.Assertions.*;

public class OrientDbRepositoryFactoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	private ODatabaseDocumentTx db;

	private OrientDbTestApplicationSettings applicationSettings = new OrientDbTestApplicationSettings();

	private Identity identity = new Identity();

	private OrientDbRepositoryFactory factory;

	@BeforeEach
	public void setUp() throws Exception {
		// open database
		db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, applicationSettings, identity);
	}

	@AfterEach
	public void tearDown() throws Exception {
		factory.getDb().close();
	}

	@Test
	public void testGetDb() throws Exception {
		assertSame(db, factory.getDb());

	}

	@Test
	public void testGetApplicationSettings() throws Exception {
		assertSame(applicationSettings, factory.getApplicationSettings());
	}

	@Test
	public void testGetIdentity() throws Exception {
		assertSame(identity, factory.getIdentity());

	}

	@Test
	public void testProduceRepository() throws Exception {
		// produce correct class
		CommentRepository commentRepository = factory.produceRepository(OrientDbCommentRepository.class);

		assertNotNull(commentRepository);
		assertTrue(commentRepository instanceof OrientDbCommentRepository);

		// produce different class
		FileRepository fileRepository = factory.produceRepository(OrientDbFileRepository.class);
		assertNotNull(fileRepository);
		assertTrue(fileRepository instanceof OrientDbFileRepository);

		// produce a second class of comment type
		CommentRepository commentRepository2 = factory.produceRepository(OrientDbCommentRepository.class);
		assertNotNull(commentRepository2);

		// should not produce a second instance
		assertSame(commentRepository, commentRepository2);


		// now produce from base class
		CommentRepository commentRepository3 = factory.produceRepository(CommentRepository.class);

		assertNotNull(commentRepository3);
		assertTrue(commentRepository3 instanceof OrientDbCommentRepository);
	}
}
