package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.File;
import org.segrada.model.prototype.IFile;
import org.segrada.service.repository.PictogramRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrientDbFileRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * repository to test
	 */
	private OrientDbFileRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		OrientDbRepositoryFactory factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbFileRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		repository.getDb().command(new OCommandSQL("delete vertex V")).execute();
		repository.getDb().command(new OCommandSQL("delete edge E")).execute();

		// close db
		try {
			repository.getDb().close();
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

		// now create an entity
		ODocument document = new ODocument("File")
				.field("title", "title")
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
}