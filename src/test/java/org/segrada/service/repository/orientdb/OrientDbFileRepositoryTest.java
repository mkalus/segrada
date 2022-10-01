package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.segrada.model.Comment;
import org.segrada.model.File;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrientDbFileRepositoryTest {
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
	private OrientDbFileRepository repository;

	@BeforeEach
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbFileRepository.class);
	}

	@AfterEach
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("delete vertex V")).execute();
		factory.getDb().command(new OCommandSQL("delete edge E")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("File", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		//TODO: test pictogram, tags

		// create an entity
		ODocument document = new ODocument("File")
				.field("title", "title")
				.field("titleasc", "title")
				.field("filename", "filename.txt")
				.field("copyright", "copyright")
				.field("mimeType", "mimeType")
				.field("location", "location")
				.field("fullText", "fullText")
				.field("fileSize", 99L)
				.field("indexFullText", true)
				.field("containFile", false)
				.field("fileIdentifier", "fileIdentifier.txt")
				.field("thumbFileIdentifier", "thumbFileIdentifier.txt")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		document.save();

		IFile file = repository.convertToEntity(document);

		assertEquals("title", file.getTitle());
		assertEquals("filename.txt", file.getFilename());
		assertEquals("copyright", file.getCopyright());
		assertEquals("mimeType", file.getMimeType());
		assertEquals("location", file.getLocation());
		assertEquals("fullText", file.getFullText());
		assertEquals(new Long(99L), file.getFileSize());
		assertEquals(true, file.getIndexFullText());
		assertEquals(false, file.getContainFile());
		assertEquals("fileIdentifier.txt", file.getFileIdentifier());
		assertEquals("thumbFileIdentifier.txt", file.getThumbFileIdentifier());
		assertEquals("Description", file.getDescription());
		assertEquals("default", file.getDescriptionMarkup());
		assertEquals(new Integer(0x123456), file.getColor());
		assertEquals(new Long(1L), file.getCreated());
		assertEquals(new Long(2L), file.getModified());

		assertEquals(document.getIdentity().toString(), file.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		//TODO: test pictogram, tags

		IFile file = new File();
		file.setTitle("title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setThumbFileIdentifier("thumbFileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		// first without id
		ODocument document = repository.convertToDocument(file);

		assertEquals("title", document.field("title"));
		assertEquals("filename.txt", document.field("filename"));
		assertEquals("copyright", document.field("copyright"));
		assertEquals("mimeType", document.field("mimeType"));
		assertEquals("location", document.field("location"));
		assertEquals("fullText", document.field("fullText"));
		assertEquals(new Long(99L), document.field("fileSize"));
		assertEquals(true, document.field("indexFullText"));
		assertEquals(false, document.field("containFile"));
		assertEquals("fileIdentifier.txt", document.field("fileIdentifier"));
		assertEquals("thumbFileIdentifier.txt", document.field("thumbFileIdentifier"));
		assertEquals("Description", document.field("description"));
		assertEquals("default", document.field("descriptionMarkup"));
		assertEquals(new Integer(0x123456), document.field("color", Integer.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		file.setId(id);

		ODocument newDocument = repository.convertToDocument(file);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindByTitleOrFilename() throws Exception {
		IFile file = new File();
		file.setTitle("title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		// empty to start with
		List<IFile> list = repository.findByTitleOrFilename("title");
		assertTrue(list.size() == 0);

		repository.save(file);

		// should be found
		list = repository.findByTitleOrFilename("title");
		assertTrue(list.size() == 1);
		assertEquals(file.getId(), list.get(0).getId());

		// also should be found searching for filename
		list = repository.findByTitleOrFilename("filename.txt");
		assertTrue(list.size() == 1);
		assertEquals(file.getId(), list.get(0).getId());

		// dummy search
		list = repository.findByTitleOrFilename("dummyTitle");
		assertTrue(list.size() == 0);
	}

	@Test
	public void testFindBySearchTerm() throws Exception {
		IFile file = new File();
		file.setTitle("This is the title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		repository.save(file);

		// empty term
		List<IFile> hits = repository.findBySearchTerm("", 1, true);
		assertEquals(1, hits.size());

		// unknown term
		hits = repository.findBySearchTerm("complexxxxxx", 1, true);
		assertEquals(0, hits.size());

		// 1 term
		hits = repository.findBySearchTerm("title", 1, true);
		assertEquals(1, hits.size());

		// filename
		hits = repository.findBySearchTerm("filename.txt", 1, true);
		assertEquals(1, hits.size());

		// 2 terms
		hits = repository.findBySearchTerm("title filename", 1, true);
		assertEquals(1, hits.size());

		// partial terms
		hits = repository.findBySearchTerm("filen tit", 1, true);
		assertEquals(1, hits.size());
	}

	@Test
	public void testProcessBeforeSaving() throws Exception {
		IFile file = new File();
		//file.setTitle("This is the title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		repository.save(file);

		assertEquals("filename.txt", file.getTitle());

		file.setTitle("This is the title");
		repository.save(file);

		assertEquals("This is the title", file.getTitle());
	}

	@Test
	public void testFindByReference() throws Exception {
		// create an entity
		ODocument document = new ODocument("File")
				.field("title", "title")
				.field("titleasc", "title")
				.field("filename", "filename.txt")
				.field("copyright", "copyright")
				.field("mimeType", "mimeType")
				.field("location", "location")
				.field("fullText", "fullText")
				.field("fileSize", 99L)
				.field("indexFullText", true)
				.field("containFile", false)
				.field("fileIdentifier", "fileIdentifier.txt")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		document.save();

		ODocument comment = new ODocument("Comment").field("text", "text")
				.field("markup", "markup").field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		comment.save();

		// no files yet
		List<IFile> files = repository.findByReference(comment.getIdentity().toString(), false);
		assertTrue(files.isEmpty());

		// create edge
		factory.getDb().command(new OCommandSQL("create edge IsFileOf from " + document.getIdentity().toString() + " to " + comment.getIdentity().toString())).execute();

		files = repository.findByReference(comment.getIdentity().toString(), false);
		assertFalse(files.isEmpty());
		assertEquals(document.getIdentity().toString(), files.get(0).getId());

		//TODO test undirected search when two files are connected
	}

	@Test
	public void testFindByFile() throws Exception {
		// create an entity
		ODocument document = new ODocument("File")
				.field("title", "title")
				.field("titleasc", "title")
				.field("filename", "filename.txt")
				.field("copyright", "copyright")
				.field("mimeType", "mimeType")
				.field("location", "location")
				.field("fullText", "fullText")
				.field("fileSize", 99L)
				.field("indexFullText", true)
				.field("containFile", false)
				.field("fileIdentifier", "fileIdentifier.txt")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		document.save();

		ODocument comment = new ODocument("Comment").field("text", "text")
				.field("markup", "markup").field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		comment.save();

		// no files yet
		List<SegradaEntity> entities = repository.findByFile(document.getIdentity().toString(), null);
		assertTrue(entities.isEmpty());

		// create edge
		factory.getDb().command(new OCommandSQL("create edge IsFileOf from " + document.getIdentity().toString() + " to " + comment.getIdentity().toString())).execute();

		entities = repository.findByFile(document.getIdentity().toString(), null);
		assertFalse(entities.isEmpty());
		assertEquals(comment.getIdentity().toString(), entities.get(0).getId());
		assertEquals("Comment", entities.get(0).getModelName());

		//TODO: test filter by classes
	}

	@Test
	public void testConnectFileToEntity() throws Exception {
		IFile file = new File();
		file.setTitle("This is the title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		repository.save(file);

		IComment comment = new Comment();
		factory.produceRepository(OrientDbCommentRepository.class).save(comment);

		// no connections, yet
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		// connect
		repository.connectFileToEntity(file, comment);

		query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		result = factory.getDb().command(query).execute();

		assertEquals(1, result.size());

		// connect again and check, if there are two connections now
		repository.connectFileToEntity(file, comment);

		query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		result = factory.getDb().command(query).execute();

		assertEquals(1, result.size());
	}

	@Test
	public void testRemoveFileFromEntity() throws Exception {
		IFile file = new File();
		file.setTitle("This is the title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		repository.save(file);

		IComment comment = new Comment();
		factory.produceRepository(OrientDbCommentRepository.class).save(comment);

		// no connections, yet
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		// create edge
		factory.getDb().command(new OCommandSQL("create edge IsFileOf from " + file.getId() + " to " + comment.getId())).execute();

		// connection exists
		query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		result = factory.getDb().command(query).execute();

		assertFalse(result.isEmpty());

		// now delete and check if there is still a connection
		repository.removeFileFromEntity(file, comment);

		// no connections anymore
		query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		// delete again, should not throw an error or so
		repository.removeFileFromEntity(file, comment);

		// no connections anymore
		query = new OSQLSynchQuery<>("select in from IsFileOf where out = " + file.getId() + " and in = " + comment.getId());
		result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());
	}

	@Test
	public void testIsFileOf() throws Exception {
		IFile file = new File();
		file.setTitle("This is the title");
		file.setFilename("filename.txt");
		file.setCopyright("copyright");
		file.setMimeType("mimeType");
		file.setLocation("location");
		file.setFullText("fullText");
		file.setFileSize(99L);
		file.setIndexFullText(true);
		file.setContainFile(false);
		file.setFileIdentifier("fileIdentifier.txt");
		file.setDescription("Description");
		file.setDescriptionMarkup("default");
		file.setColor(0x123456);
		file.setCreated(1L);
		file.setModified(2L);

		repository.save(file);

		IComment comment = new Comment();
		factory.produceRepository(OrientDbCommentRepository.class).save(comment);

		// no files yet
		assertFalse(repository.isFileOf(file, comment));

		// create edge
		factory.getDb().command(new OCommandSQL("create edge IsFileOf from " + file.getId() + " to " + comment.getId())).execute();

		assertTrue(repository.isFileOf(file, comment));
	}

	@Test
	public void testPaginate() throws Exception {
		//fail("Test not implemented yet.");
		//TODO: do later
	}
}
