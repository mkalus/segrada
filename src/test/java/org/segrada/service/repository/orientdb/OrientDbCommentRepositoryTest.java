package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Comment;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.*;

public class OrientDbCommentRepositoryTest {
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
	private OrientDbCommentRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbCommentRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("delete edge IsCommentOf")).execute();
		factory.getDb().command(new OCommandSQL("delete vertex Comment")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("Comment", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument document = new ODocument("Comment").field("text", "text")
				.field("markup", "markup").field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		document.save();

		IComment comment = repository.convertToEntity(document);

		assertEquals("text", comment.getText());
		assertEquals("markup", comment.getMarkup());
		assertEquals(new Long(1L), comment.getCreated());
		assertEquals(new Long(2L), comment.getModified());
		assertEquals(document.getIdentity().toString(), comment.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		IComment comment = new Comment();
		comment.setText("text");
		comment.setMarkup("markup");

		// first without id
		ODocument document = repository.convertToDocument(comment);

		assertEquals("text", document.field("text"));
		assertEquals("markup", document.field("markup"));

		// class name should be correct
		assertEquals("Comment", document.getClassName());

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		comment.setId(id);

		ODocument newDocument = repository.convertToDocument(comment);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindByReference() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		IComment comment3 = new Comment();
		comment3.setText("Comment 3");
		comment3.setMarkup("markup");
		repository.save(comment3);

		// empty to start with
		List<IComment> list = repository.findByReference(comment1.getId());
		assertTrue(list.size() == 0);

		repository.connectCommentToEntity(comment2, comment1);
		repository.connectCommentToEntity(comment3, comment1);

		list = repository.findByReference(comment1.getId());
		assertTrue(list.size() == 2);

		assertEquals(comment2.getId(), list.get(0).getId());
		assertEquals(comment3.getId(), list.get(1).getId());
	}


	@Test
	public void testFindByComment() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		IComment comment3 = new Comment();
		comment3.setText("Comment 3");
		comment3.setMarkup("markup");
		repository.save(comment3);

		// empty to start with
		List<SegradaEntity> list = repository.findByComment(comment1.getId());
		assertTrue(list.size() == 0);

		repository.connectCommentToEntity(comment1, comment2);
		repository.connectCommentToEntity(comment1, comment3);

		list = repository.findByComment(comment1.getId());
		assertTrue(list.size() == 2);

		assertEquals(comment2.getId(), list.get(0).getId());
		assertEquals(comment3.getId(), list.get(1).getId());
	}

	@Test
	public void testConnectCommentWith() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		// there should be no connection to begin with
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		repository.connectCommentToEntity(comment1, comment2);

		// there should be one connection
		query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		result = factory.getDb().command(query).execute();

		assertFalse(result.isEmpty());

		// connect again
		repository.connectCommentToEntity(comment1, comment2);

		// there should be one connection only
		query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		result = factory.getDb().command(query).execute();

		assertEquals(1, result.size());
	}

	@Test
	public void testDeleteCommentConnection() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		// there should be no connection to begin with
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		repository.connectCommentToEntity(comment1, comment2);

		// there should be one connection
		query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		result = factory.getDb().command(query).execute();

		assertFalse(result.isEmpty());

		// ok, now delete edge
		repository.removeCommentFromEntity(comment1, comment2);

		// now check of edge was deleted
		query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		// ok, now delete edge again - should work
		repository.removeCommentFromEntity(comment1, comment2);

		// now check of edge was deleted
		query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());
	}

	@Test
	public void testHasConnections() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		// no connections to begin with
		assertFalse(repository.hasConnections(comment1));

		repository.connectCommentToEntity(comment1, comment2);

		// is connected
		assertTrue(repository.hasConnections(comment1));
		// but not vice versa
		assertFalse(repository.hasConnections(comment2));
	}
}