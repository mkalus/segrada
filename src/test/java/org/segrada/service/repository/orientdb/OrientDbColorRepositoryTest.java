package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Color;
import org.segrada.model.prototype.IColor;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import static org.junit.Assert.assertEquals;

public class OrientDbColorRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * repository to test
	 */
	private OrientDbColorRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// create repository
		repository = new OrientDbColorRepository(db, new OrientDbTestApplicationSettings(), new Identity());
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		repository.getDb().command(new OCommandSQL("truncate class Color")).execute();

		// close db
		try {
			repository.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("Color", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument document = new ODocument("Color").field("title", "title")
				.field("color", 123456).field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		IColor color = repository.convertToEntity(document);

		assertEquals("title", color.getTitle());
		assertEquals(new Integer(123456), color.getColor());
		assertEquals(new Long(1L), color.getCreated());
		assertEquals(new Long(2L), color.getModified());
		assertEquals(document.getIdentity().toString(), color.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		IColor color = new Color();
		color.setTitle("title");
		color.setColor(123456);
		color.setCreated(1L);
		color.setModified(2L);

		// first without id
		ODocument document = repository.convertToDocument(color);

		assertEquals("title", document.field("title"));
		assertEquals(new Integer(123456), document.field("color"));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		// class name should be correct
		assertEquals("Color", document.getClassName());

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		color.setId(id);

		ODocument newDocument = repository.convertToDocument(color);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY color", repository.getDefaultOrder(true));
		assertEquals(" color", repository.getDefaultOrder(false));
	}
}