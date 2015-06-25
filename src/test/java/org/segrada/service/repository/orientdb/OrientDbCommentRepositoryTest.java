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
import org.segrada.model.util.IdModelTuple;
import org.segrada.service.repository.FileRepository;
import org.segrada.service.repository.SourceReferenceRepository;
import org.segrada.service.repository.SourceRepository;
import org.segrada.service.repository.TagRepository;
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
	 * repository to test
	 */
	private OrientDbCommentRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		OrientDbRepositoryFactory factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbCommentRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		repository.getDb().command(new OCommandSQL("delete edge IsCommentOf")).execute();
		repository.getDb().command(new OCommandSQL("delete vertex Comment")).execute();

		// close db
		try {
			repository.getDb().close();
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

		assertTrue(repository.connectCommentWith(comment2, comment1));
		assertTrue(repository.connectCommentWith(comment3, comment1));

		list = repository.findByReference(comment1.getId());
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

		assertTrue(repository.connectCommentWith(comment1, comment2));
		// twice should return false
		assertFalse(repository.connectCommentWith(comment1, comment2));

		// now check of edge was created
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		List<ODocument> result = repository.getDb().command(query).execute();

		assertTrue(result.size() == 1);

		// also check empty sets (no ids null values, etc.)
		//TODO
	}

	@Test
	public void testConnectCommentWithTuple() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		// create tuple
		IdModelTuple idModelTuple = new IdModelTuple(comment2.getId(), "Comment");

		assertTrue(repository.connectCommentWith(comment1, idModelTuple));
		// twice should return false
		assertFalse(repository.connectCommentWith(comment1, idModelTuple));

		// now check of edge was created
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		List<ODocument> result = repository.getDb().command(query).execute();

		assertTrue(result.size() == 1);

		// also check empty sets (no ids null values, etc.)
		//TODO
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

		assertTrue(repository.connectCommentWith(comment1, comment2));

		// ok, now delete edge again
		assertTrue(repository.deleteCommentConnection(comment1, comment2));
		// twice should return false
		assertFalse(repository.deleteCommentConnection(comment1, comment2));

		// now check of edge was deleted
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		List<ODocument> result = repository.getDb().command(query).execute();

		assertTrue(result.size() == 0);
	}

	@Test
	public void testDeleteCommentConnectionTuple() throws Exception {
		IComment comment1 = new Comment();
		comment1.setText("Comment 1");
		comment1.setMarkup("markup");
		repository.save(comment1);

		IComment comment2 = new Comment();
		comment2.setText("Comment 2");
		comment2.setMarkup("markup");
		repository.save(comment2);

		assertTrue(repository.connectCommentWith(comment1, comment2));

		// create tuple
		IdModelTuple idModelTuple = new IdModelTuple(comment2.getId(), "Comment");

		// ok, now delete edge again
		assertTrue(repository.deleteCommentConnection(comment1, idModelTuple));
		// twice should return false
		assertFalse(repository.deleteCommentConnection(comment1, idModelTuple));

		// now check of edge was deleted
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment1.getId() + " AND in = " + comment2.getId());
		List<ODocument> result = repository.getDb().command(query).execute();

		assertTrue(result.size() == 0);
	}

	@Test
	public void testGetConnectedIdModelTuplesOf() throws Exception {
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
		List<IdModelTuple> list = repository.getConnectedIdModelTuplesOf(comment1);
		assertTrue(list.size() == 0);

		assertTrue(repository.connectCommentWith(comment1, comment2));
		assertTrue(repository.connectCommentWith(comment1, comment3));

		list = repository.getConnectedIdModelTuplesOf(comment1);
		assertTrue(list.size() == 2);

		assertTrue(list.get(0).model.equals("Comment"));
		assertTrue(list.get(1).model.equals("Comment"));
		assertTrue(list.get(0).id.equals(comment2.getId()));
		assertTrue(list.get(1).id.equals(comment3.getId()));
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

		assertTrue(repository.connectCommentWith(comment1, comment2));

		// is connected
		assertTrue(repository.hasConnections(comment1));
		// but not vice versa
		assertFalse(repository.hasConnections(comment2));
	}
}