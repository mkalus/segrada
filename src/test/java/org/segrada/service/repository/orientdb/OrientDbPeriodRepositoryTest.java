package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Period;
import org.segrada.model.prototype.IPeriod;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrientDbPeriodRepositoryTest {

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
	private OrientDbPeriodRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbPeriodRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("delete vertex V")).execute();
		factory.getDb().command(new OCommandSQL("truncate class Period")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("Period", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument parent = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent.save();

		ODocument document = new ODocument("Period")
				.field("fromEntryCalendar", "G").field("toEntryCalendar", "G")
				.field("fromEntry", "1.1585").field("toEntry", "2.1585")
				.field("from", 2299970L).field("to", 2300028L)
				.field("type", "period")
				.field("parent", parent).field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		IPeriod period = repository.convertToEntity(document);

		assertEquals("1.1585", period.getFromEntry());
		assertEquals("2.1585", period.getToEntry());
		assertEquals("G", period.getFromEntryCalendar());
		assertEquals("G", period.getToEntryCalendar());
		assertEquals("period", period.getType());
		assertEquals(new Long(2299970L), period.getFromJD());
		assertEquals(new Long(2300028L), period.getToJD());
		assertEquals(new Long(1L), period.getCreated());
		assertEquals(new Long(2L), period.getModified());
		assertEquals(parent.getIdentity().toString(), period.getParentId());
		assertEquals("Node", period.getParentModel());
		assertEquals(document.getIdentity().toString(), period.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		ODocument parent = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent.save();

		IPeriod period = new Period();
		period.setFromEntry("1.1585");
		period.setToEntry("2.1585");
		period.setCreated(1L);
		period.setModified(2L);
		period.setParentId(parent.getIdentity().toString());
		period.setParentModel("Node");

		// first without id
		ODocument document = repository.convertToDocument(period);

		assertEquals("1.1585", document.field("fromEntry"));
		assertEquals("2.1585", document.field("toEntry"));
		assertEquals("G", document.field("fromEntryCalendar"));
		assertEquals("G", document.field("toEntryCalendar"));
		assertEquals(new Long(2299970L), document.field("fromJD", Long.class));
		assertEquals(new Long(2300028L), document.field("toJD", Long.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));
		assertEquals(parent.getIdentity().toString(), document.field("parent", String.class));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		period.setId(id);

		ODocument newDocument = repository.convertToDocument(period);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
		assertEquals(parent.getIdentity(), newDocument.field("parent", ORecordId.class));
	}

	@Test
	public void testFindByParent() throws Exception {
		ODocument parent1 = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent1.save();
		ODocument parent2 = new ODocument("Node").field("title", "ref2").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent2.save();

		String id1 = parent1.getIdentity().toString();
		String id2 = parent2.getIdentity().toString();

		IPeriod period1 = new Period();
		period1.setFromEntry("1.1585");
		period1.setToEntry("2.1585");
		period1.setCreated(1L);
		period1.setModified(2L);
		period1.setParentId(id1);
		repository.save(period1);

		IPeriod period2 = new Period();
		period2.setFromEntry("1929");
		period2.setToEntry("1930");
		period2.setCreated(1L);
		period2.setModified(2L);
		period2.setParentId(id1);
		repository.save(period2);

		IPeriod period3 = new Period();
		period3.setFromEntry("1.1.1700");
		period3.setToEntry("5.6.1702");
		period3.setCreated(1L);
		period3.setModified(2L);
		period3.setParentId(id1);
		repository.save(period3);

		List<IPeriod> periods = repository.findByParent(id1);

		assertTrue(periods.size() == 3);

		periods = repository.findByParent(id2);
		assertTrue(periods.size() == 0);
	}

	@Test
	public void testFindWithin() throws Exception {
		ODocument parent1 = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent1.save();
		ODocument parent2 = new ODocument("Node").field("title", "ref2").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent2.save();

		String id1 = parent1.getIdentity().toString();
		String id2 = parent2.getIdentity().toString();

		IPeriod period1 = new Period();
		period1.setFromEntry("1.1585");
		period1.setToEntry("2.1585");
		period1.setCreated(1L);
		period1.setModified(2L);
		period1.setParentId(id1);
		repository.save(period1);

		IPeriod period2 = new Period();
		period2.setFromEntry("1929");
		period2.setToEntry("1930");
		period2.setCreated(1L);
		period2.setModified(2L);
		period2.setParentId(id1);
		repository.save(period2);

		IPeriod period3 = new Period();
		period3.setFromEntry("1.1.1700");
		period3.setToEntry("5.6.1702");
		period3.setCreated(1L);
		period3.setModified(2L);
		period3.setParentId(id2);
		repository.save(period3);

		IPeriod period4 = new Period();
		period4.setFromEntry(null);
		period4.setToEntry("1596");
		period4.setCreated(1L);
		period4.setModified(2L);
		period4.setParentId(id1);
		repository.save(period4);

		IPeriod period5 = new Period();
		period5.setFromEntry("5.1.901");
		period5.setToEntry(null);
		period5.setCreated(1L);
		period5.setModified(2L);
		period5.setParentId(id1);
		repository.save(period5);

		IPeriod period6 = new Period();
		period6.setFromEntry("19.6.1601");
		period6.setToEntry("19.6.1601");
		period6.setCreated(1L);
		period6.setModified(2L);
		period6.setParentId(id2);
		repository.save(period6);

		List<IPeriod> periods;

		// first, the simple cases:
		// all entries up to 18-6-1601
		periods = repository.findWithin(null, 2305982L);
		assertEquals(3, periods.size());
		assertEquals(null, periods.get(0).getFromEntry());
		assertEquals("1596", periods.get(0).getToEntry());
		assertEquals("5.1.901", periods.get(1).getFromEntry());
		assertEquals(null, periods.get(1).getToEntry());
		assertEquals("1.1585", periods.get(2).getFromEntry());
		assertEquals("2.1585", periods.get(2).getToEntry());

		// all entries on and after 1-1-1585
		periods = repository.findWithin(2299970L, null);
		assertEquals(5, periods.size());
		assertEquals(null, periods.get(0).getFromEntry());
		assertEquals("1596", periods.get(0).getToEntry());
		assertEquals("1.1585", periods.get(1).getFromEntry());
		assertEquals("2.1585", periods.get(1).getToEntry());
		assertEquals("19.6.1601", periods.get(2).getFromEntry());
		assertEquals("19.6.1601", periods.get(2).getToEntry());
		assertEquals("1.1.1700", periods.get(3).getFromEntry());
		assertEquals("5.6.1702", periods.get(3).getToEntry());
		assertEquals("1929", periods.get(4).getFromEntry());
		assertEquals("1930", periods.get(4).getToEntry());

		//1-1-1585 to 18-6-1601
		periods = repository.findWithin(2299970L, 2305982L);
		assertEquals(3, periods.size());
		assertEquals(null, periods.get(0).getFromEntry());
		assertEquals("1596", periods.get(0).getToEntry());
		assertEquals("5.1.901", periods.get(1).getFromEntry());
		assertEquals(null, periods.get(1).getToEntry());
		assertEquals("1.1585", periods.get(2).getFromEntry());
		assertEquals("2.1585", periods.get(2).getToEntry());
	}

	@Test
	public void testFindWithin1() throws Exception {
		ODocument parent1 = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent1.save();
		ODocument parent2 = new ODocument("Node").field("title", "ref2").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent2.save();

		String id1 = parent1.getIdentity().toString();
		String id2 = parent2.getIdentity().toString();

		IPeriod period1 = new Period();
		period1.setFromEntry("1.1585");
		period1.setToEntry("2.1585");
		period1.setCreated(1L);
		period1.setModified(2L);
		period1.setParentId(id1);
		repository.save(period1);

		IPeriod period2 = new Period();
		period2.setFromEntry("1929");
		period2.setToEntry("1930");
		period2.setCreated(1L);
		period2.setModified(2L);
		period2.setParentId(id1);
		repository.save(period2);

		IPeriod period3 = new Period();
		period3.setFromEntry("1.1.1700");
		period3.setToEntry("5.6.1702");
		period3.setCreated(1L);
		period3.setModified(2L);
		period3.setParentId(id2);
		repository.save(period3);

		IPeriod period4 = new Period();
		period4.setFromEntry(null);
		period4.setToEntry("1596");
		period4.setCreated(1L);
		period4.setModified(2L);
		period4.setParentId(id1);
		repository.save(period4);

		IPeriod period5 = new Period();
		period5.setFromEntry("5.1.901");
		period5.setToEntry(null);
		period5.setCreated(1L);
		period5.setModified(2L);
		period5.setParentId(id1);
		repository.save(period5);

		IPeriod period6 = new Period();
		period6.setFromEntry("19.6.1601");
		period6.setToEntry("19.6.1601");
		period6.setCreated(1L);
		period6.setModified(2L);
		period6.setParentId(id2);
		repository.save(period6);

		//1-1-1585 to 18-6-1601
		DateTime start = new DateTime().withYear(1585).withMonthOfYear(1).withDayOfMonth(1).withTime(0,0,0,0);
		DateTime end = new DateTime().withYear(1601).withMonthOfYear(6).withDayOfMonth(18).withTime(0,0,0,0);

		List<IPeriod> periods;

		periods = repository.findWithin(start, end);
		assertEquals(3, periods.size());
		assertEquals(null, periods.get(0).getFromEntry());
		assertEquals("1596", periods.get(0).getToEntry());
		assertEquals("5.1.901", periods.get(1).getFromEntry());
		assertEquals(null, periods.get(1).getToEntry());
		assertEquals("1.1585", periods.get(2).getFromEntry());
		assertEquals("2.1585", periods.get(2).getToEntry());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY fromJD ASC, toJD ASC", repository.getDefaultOrder(true));
		assertEquals(" fromJD ASC, toJD ASC", repository.getDefaultOrder(false));

	}
}