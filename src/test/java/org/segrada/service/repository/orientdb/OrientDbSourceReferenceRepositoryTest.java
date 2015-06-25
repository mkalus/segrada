package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Comment;
import org.segrada.model.Source;
import org.segrada.model.SourceReference;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrientDbSourceReferenceRepositoryTest {

	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * repository to test
	 */
	private OrientDbSourceReferenceRepository repository;

	/**
	 * reference to factory
	 */
	private OrientDbRepositoryFactory factory;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbSourceReferenceRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("delete edge E")).execute();
		factory.getDb().command(new OCommandSQL("delete vertex V")).execute();
		factory.getDb().command(new OCommandSQL("truncate class SourceReference")).execute();
		factory.getDb().command(new OCommandSQL("truncate class Source")).execute();
		factory.getDb().command(new OCommandSQL("truncate class Comment")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("SourceReference", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		OrientDbCommentRepository commentRepository = factory.produceRepository(OrientDbCommentRepository.class);
		OrientDbSourceRepository sourceRepository = factory.produceRepository(OrientDbSourceRepository.class);

		// create comment to connect to
		IComment comment = new Comment();
		comment.setText("Comment Text");
		commentRepository.save(comment);

		// create source to connect to
		ISource source = new Source();
		source.setShortTitle("ShortTitle");
		source.setShortRef("ref:ref");
		sourceRepository.save(source);

		// create reference
		ISourceReference sourceReference = new SourceReference();
		sourceReference.setReferenceText("pp. 11f");
		sourceReference.setSource(source);
		sourceReference.setReference(comment);

		repository.save(sourceReference);

		// now get ODocument from db
		ODocument document = factory.getDb().load(new ORecordId(sourceReference.getId()));

		ISourceReference referenceToCheck = repository.convertToEntity(document);
		assertEquals(sourceReference.getSource().getId(), referenceToCheck.getSource().getId());
		assertEquals(sourceReference.getReference().getId(), referenceToCheck.getReference().getId());
		assertEquals(sourceReference.getReferenceText(), referenceToCheck.getReferenceText());
		assertEquals(sourceReference.getCreated(), referenceToCheck.getCreated());
		assertEquals(sourceReference.getModified(), referenceToCheck.getModified());
		assertEquals(sourceReference.getCreator(), referenceToCheck.getCreator());
		assertEquals(sourceReference.getId(), referenceToCheck.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		OrientDbCommentRepository commentRepository = factory.produceRepository(OrientDbCommentRepository.class);
		OrientDbSourceRepository sourceRepository = factory.produceRepository(OrientDbSourceRepository.class);

		// create comment to connect to
		IComment comment = new Comment();
		comment.setText("Comment Text");
		commentRepository.save(comment);

		// create source to connect to
		ISource source = new Source();
		source.setShortTitle("ShortTitle");
		source.setShortRef("ref:ref");
		sourceRepository.save(source);

		// create reference
		ISourceReference sourceReference = new SourceReference();
		sourceReference.setReferenceText("pp. 11f");
		sourceReference.setSource(source);
		sourceReference.setReference(comment);
		sourceReference.setCreated(1L);
		sourceReference.setModified(2L);

		// first without id
		ODocument document = repository.convertToDocument(sourceReference);

		assertEquals("pp. 11f", document.field("referenceText"));
		assertEquals(source.getId(), ((ORecordId) document.field("source", ORecordId.class)).toString());
		assertEquals(comment.getId(), ((ORecordId) document.field("reference", ORecordId.class)).toString());
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		sourceReference.setId(id);

		ODocument newDocument = repository.convertToDocument(sourceReference);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindBySource() throws Exception {
		OrientDbCommentRepository commentRepository = factory.produceRepository(OrientDbCommentRepository.class);
		OrientDbSourceRepository sourceRepository = factory.produceRepository(OrientDbSourceRepository.class);

		// create comment to connect to
		IComment comment = new Comment();
		comment.setText("Comment Text");
		commentRepository.save(comment);

		// create source to connect to
		ISource source = new Source();
		source.setShortTitle("ShortTitle");
		source.setShortRef("ref:ref");
		sourceRepository.save(source);

		// create reference
		ISourceReference sourceReference = new SourceReference();
		sourceReference.setReferenceText("pp. 11f");
		sourceReference.setSource(source);
		sourceReference.setReference(comment);

		repository.save(sourceReference);

		// do not find anything when dummy searching
		List<ISourceReference> list = repository.findBySource("99:99");
		assertTrue(list.isEmpty());

		// find by reference
		list = repository.findBySource(source.getId());
		assertTrue(list.size() == 1);
		assertEquals(sourceReference.getId(), list.get(0).getId());
	}

	@Test
	public void testFindByReference() throws Exception {
		OrientDbCommentRepository commentRepository = factory.produceRepository(OrientDbCommentRepository.class);
		OrientDbSourceRepository sourceRepository = factory.produceRepository(OrientDbSourceRepository.class);

		// create comment to connect to
		IComment comment = new Comment();
		comment.setText("Comment Text");
		commentRepository.save(comment);

		// create source to connect to
		ISource source = new Source();
		source.setShortTitle("ShortTitle");
		source.setShortRef("ref:ref");
		sourceRepository.save(source);

		// create reference
		ISourceReference sourceReference = new SourceReference();
		sourceReference.setReferenceText("pp. 11f");
		sourceReference.setSource(source);
		sourceReference.setReference(comment);

		repository.save(sourceReference);

		// do not find anything when dummy searching
		List<ISourceReference> list = repository.findByReference("99:99");
		assertTrue(list.isEmpty());

		// find by reference
		list = repository.findByReference(comment.getId());
		assertTrue(list.size() == 1);
		assertEquals(sourceReference.getId(), list.get(0).getId());
	}
}