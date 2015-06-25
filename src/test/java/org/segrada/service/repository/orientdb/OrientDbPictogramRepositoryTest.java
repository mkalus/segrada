package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Pictogram;
import org.segrada.model.prototype.IPictogram;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.*;

public class OrientDbPictogramRepositoryTest {
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
	private OrientDbPictogramRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbPictogramRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("truncate class Pictogram")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("Pictogram", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument document = new ODocument("Pictogram").field("title", "title")
				.field("fileIdentifier", "test.txt")
				.field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		IPictogram pictogram = repository.convertToEntity(document);

		assertEquals("title", pictogram.getTitle());
		assertEquals("test.txt", pictogram.getFileIdentifier());
		assertEquals(new Long(1L), pictogram.getCreated());
		assertEquals(new Long(2L), pictogram.getModified());
		assertEquals(document.getIdentity().toString(), pictogram.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		IPictogram pictogram = new Pictogram();
		pictogram.setTitle("title");
		pictogram.setFileIdentifier("test.txt");

		ODocument document = repository.convertToDocument(pictogram);

		assertEquals("title", document.field("title"));
		assertEquals("test.txt", document.field("fileIdentifier"));

		// class name should be correct
		assertEquals("Pictogram", document.getClassName());

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		pictogram.setId(id);

		ODocument newDocument = repository.convertToDocument(pictogram);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindByTitle() throws Exception {
		IPictogram pictogram = new Pictogram();
		pictogram.setTitle("testFindByTitle");
		pictogram.setFileIdentifier("test.txt");

		repository.save(pictogram);

		IPictogram testPictogram = repository.findByTitle(pictogram.getTitle());

		assertEquals(pictogram.getId(), testPictogram.getId());

		// return null for non-existing entities
		testPictogram = repository.findByTitle("NON-EXIST");

		assertEquals(null, testPictogram);
	}

	@Test
	public void testFindBySearchTerm() throws Exception {
		IPictogram pictogram = new Pictogram();
		pictogram.setTitle("This is a complex title with an Ümlaut");
		pictogram.setFileIdentifier("test.txt");

		repository.save(pictogram);

		// empty term
		List<IPictogram> hits = repository.findBySearchTerm("", 1, true);
		assertEquals(1, hits.size());

		// unknown term
		hits = repository.findBySearchTerm("complexxxxxx", 1, true);
		assertEquals(0, hits.size());

		// 1 term
		hits = repository.findBySearchTerm("complex", 1, true);
		assertEquals(1, hits.size());

		// 2 terms
		hits = repository.findBySearchTerm("Title complex", 1, true);
		assertEquals(1, hits.size());

		// partial terms
		hits = repository.findBySearchTerm("comp tit", 1, true);
		assertEquals(1, hits.size());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY title", repository.getDefaultOrder(true));
		assertEquals(" title", repository.getDefaultOrder(false));
	}
}