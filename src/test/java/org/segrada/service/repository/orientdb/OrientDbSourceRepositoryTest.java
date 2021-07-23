package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Comment;
import org.segrada.model.Period;
import org.segrada.model.Source;
import org.segrada.model.SourceReference;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.service.repository.CommentRepository;
import org.segrada.service.repository.SourceReferenceRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrientDbSourceRepositoryTest {
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
	private OrientDbSourceRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbSourceRepository.class);
	}

	@After
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
		assertEquals("Source", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		//TODO: test pictogram, tags

		// now create an entity
		ODocument document = new ODocument("Source")
				.field("shortTitle", "shortTitle")
				.field("shortTitleasc", "shortTitle")
				.field("shortRef", "shortRef")
				.field("sourceType", "sourceType")
				.field("url", "url")
				.field("productCode", "productCode")
				.field("author", "author")
				.field("citation", "citation")
				.field("longTitle", "longTitle")
				.field("copyright", "copyright")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		document.save();

		ISource node = repository.convertToEntity(document);

		assertEquals("shortTitle", node.getShortTitle());
		assertEquals("sourceType", node.getSourceType());
		assertEquals("shortRef", node.getShortRef());
		assertEquals("url", node.getUrl());
		assertEquals("productCode", node.getProductCode());
		assertEquals("author", node.getAuthor());
		assertEquals("citation", node.getCitation());
		assertEquals("longTitle", node.getTitle());
		assertEquals("copyright", node.getCopyright());
		assertEquals("Description", node.getDescription());
		assertEquals("default", node.getDescriptionMarkup());
		assertEquals(new Integer(0x123456), node.getColor());
		assertEquals(new Long(1L), node.getCreated());
		assertEquals(new Long(2L), node.getModified());

		assertEquals(document.getIdentity().toString(), node.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		//TODO: test pictogram, tags

		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setSourceType("sourceType");
		source.setShortRef("shortRef");
		source.setUrl("url");
		source.setProductCode("productCode");
		source.setAuthor("author");
		source.setCitation("citation");
		source.setLongTitle("longTitle");
		source.setCopyright("copyright");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");
		source.setColor(0x123456);
		source.setCreated(1L);
		source.setModified(2L);

		List<IPeriod> list = new ArrayList<>();

		// add a few periods
		IPeriod period1 = new Period();
		period1.setFromEntry("1.1585");
		period1.setToEntry("2.1585");
		period1.setCreated(1L);
		period1.setModified(2L);
		list.add(period1);

		IPeriod period2 = new Period();
		period2.setFromEntry("1929");
		period2.setToEntry("1930");
		period2.addFuzzyFromFlag('c');
		period2.addFuzzyToFlag('?');
		period2.setCreated(1L);
		period2.setModified(2L);
		list.add(period2);

		IPeriod period3 = new Period();
		period3.setFromEntry("1.1.1700");
		period3.setToEntry("5.6.1702");
		period3.setCreated(1L);
		period3.setModified(2L);
		list.add(period3);

		IPeriod period4 = new Period();
		period4.setFromEntry(null);
		period4.setToEntry("1596");
		period4.addFuzzyToFlag('c');
		period4.setCreated(1L);
		period4.setModified(2L);
		list.add(period4);

		IPeriod period5 = new Period();
		period5.setFromEntry("5.1.901");
		period5.setToEntry(null);
		period5.addFuzzyFromFlag('?');
		period5.setCreated(1L);
		period5.setModified(2L);
		list.add(period5);

		IPeriod period6 = new Period();
		period6.setFromEntry("19.6.1601");
		period6.setToEntry("19.6.1601");
		period6.setCreated(1L);
		period6.setModified(2L);
		list.add(period6);

		// add periods
		source.setPeriods(list);

		// first without id
		ODocument document = repository.convertToDocument(source);

		assertEquals("shortTitle", document.field("shortTitle"));
		assertEquals("sourceType", document.field("sourceType"));
		assertEquals("shortRef", document.field("shortRef"));
		assertEquals("url", document.field("url"));
		assertEquals("productCode", document.field("productCode"));
		assertEquals("author", document.field("author"));
		assertEquals("citation", document.field("citation"));
		assertEquals("longTitle", document.field("longTitle"));
		assertEquals("copyright", document.field("copyright"));
		assertEquals("Description", document.field("description"));
		assertEquals("default", document.field("descriptionMarkup"));
		assertEquals(new Integer(0x123456), document.field("color", Integer.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		assertEquals(period5.getFromJD(), document.field("minJD"));
		assertEquals(period2.getToJD(), document.field("maxJD"));
		assertEquals(period5.getFromEntry(), document.field("minEntry"));
		assertEquals(period2.getToEntry(), document.field("maxEntry"));
		assertEquals(period5.getFromEntryCalendar(), document.field("minEntryCalendar"));
		assertEquals(period2.getToEntryCalendar(), document.field("maxEntryCalendar"));
		assertEquals("?", document.field("minFuzzyFlags"));
		assertEquals("?", document.field("maxFuzzyFlags"));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		source.setId(id);

		ODocument newDocument = repository.convertToDocument(source);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindByRef() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("ref:ref");
		source.setUrl("url");
		source.setProductCode("productCode");
		source.setAuthor("author");
		source.setCitation("citation");
		source.setLongTitle("title");
		source.setCopyright("copyright");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");
		source.setColor(0x123456);
		source.setCreated(1L);
		source.setModified(2L);

		// empty to start with
		ISource found = repository.findByRef("ref:ref");
		assertNull(found);

		repository.save(source);

		// should be found
		found = repository.findByRef("ref:ref");
		assertNotNull(found);
		assertEquals(source.getId(), found.getId());

		// dummy search
		found = repository.findByRef("dummy:dummy");
		assertNull(found);
	}

	@Test
	public void testFindByTitle() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setUrl("url");
		source.setProductCode("productCode");
		source.setAuthor("author");
		source.setCitation("citation");
		source.setLongTitle("title");
		source.setCopyright("copyright");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");
		source.setColor(0x123456);
		source.setCreated(1L);
		source.setModified(2L);

		// empty to start with
		List<ISource> list = repository.findByTitle("title");
		assertTrue(list.size() == 0);

		repository.save(source);

		// should be found
		list = repository.findByTitle("title");
		assertTrue(list.size() == 1);
		assertEquals(source.getId(), list.get(0).getId());

		// also should be found searching for shortTitle
		list = repository.findByTitle("shortTitle");
		assertTrue(list.size() == 1);
		assertEquals(source.getId(), list.get(0).getId());

		// dummy search
		list = repository.findByTitle("dummyTitle");
		assertTrue(list.size() == 0);
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY shortTitleAsc", repository.getDefaultOrder(true));
		assertEquals(" shortTitleAsc", repository.getDefaultOrder(false));
	}

	@Test
	public void testFindBySearchTerm() throws Exception {
		ISource source = new Source();
		source.setShortTitle("This is the short title");
		source.setShortRef("ref:ref");
		source.setUrl("url");
		source.setProductCode("productCode");
		source.setAuthor("author");
		source.setCitation("citation");
		source.setLongTitle("This is the title");
		source.setCopyright("copyright");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");
		source.setColor(0x123456);
		source.setCreated(1L);
		source.setModified(2L);

		repository.save(source);

		// empty term
		List<ISource> hits = repository.findBySearchTerm("", 1, true);
		assertEquals(1, hits.size());

		// unknown term
		hits = repository.findBySearchTerm("complexxxxxx", 1, true);
		assertEquals(0, hits.size());

		// 1 term
		hits = repository.findBySearchTerm("title", 1, true);
		assertEquals(1, hits.size());

		// short title
		hits = repository.findBySearchTerm("short", 1, true);
		assertEquals(1, hits.size());

		// short reference
		hits = repository.findBySearchTerm("ref:ref", 1, true);
		assertEquals(1, hits.size());

		// 2 terms
		hits = repository.findBySearchTerm("title ref", 1, true);
		assertEquals(1, hits.size());

		// partial terms
		hits = repository.findBySearchTerm("sho tit", 1, true);
		assertEquals(1, hits.size());
	}

	@Test
	public void testDelete() throws Exception {
		CommentRepository commentRepository = factory.produceRepository(OrientDbCommentRepository.class);
		SourceReferenceRepository sourceReferenceRepository = factory.produceRepository(OrientDbSourceReferenceRepository.class);

		ISource source = new Source();
		repository.save(source);

		IComment comment = new Comment();
		comment.setText("Comment Text");
		commentRepository.save(comment);

		// create reference
		ISourceReference sourceReference = new SourceReference();
		sourceReference.setReferenceText("pp. 11f");
		sourceReference.setSource(source);
		sourceReference.setReference(comment);
		sourceReferenceRepository.save(sourceReference);

		// now delete source
		repository.delete(source);

		// check, if source reference has been deleted, too
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from " + sourceReference.getId());
		List<ODocument> result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());

		// comment should not have been deleted
		query = new OSQLSynchQuery<>("select * from " + comment.getId());
		result = factory.getDb().command(query).execute();
		assertFalse(result.isEmpty());
	}

	@Test
	public void testPaginate() throws Exception {
		//fail("Test not implemented yet.");
		//TODO: do later
	}
}