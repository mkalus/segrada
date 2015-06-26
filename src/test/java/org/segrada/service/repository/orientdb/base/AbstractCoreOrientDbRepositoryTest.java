package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Location;
import org.segrada.model.Node;
import org.segrada.model.Period;
import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.service.repository.LocationRepository;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.PeriodRepository;
import org.segrada.service.repository.orientdb.OrientDbLocationRepository;
import org.segrada.service.repository.orientdb.OrientDbNodeRepository;
import org.segrada.service.repository.orientdb.OrientDbPeriodRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractCoreOrientDbRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * reference to factory
	 */
	private OrientDbRepositoryFactory factory;

	/**
	 * mock repository - see below
	 */
	private MockOrientDbRepository mockOrientDbRepository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// create schema
		db.command(new OCommandSQL("create class Mock extends V")).execute();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repo
		mockOrientDbRepository = new MockOrientDbRepository(factory);
	}

	@After
	public void tearDown() throws Exception {
		// close db
		try {
			mockOrientDbRepository.db.close();
		} catch (Exception e) {
			// do nothing
		}

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		// remove schema
		db.command(new OCommandSQL("delete vertex V")).execute();
		db.command(new OCommandSQL("delete edge E")).execute();
		db.command(new OCommandSQL("truncate class Pictogram")).execute();
		db.command(new OCommandSQL("truncate class Location")).execute();
		db.command(new OCommandSQL("truncate class Period")).execute();
		db.command(new OCommandSQL("drop class Mock")).execute();

		// close db
		db.close();
	}

	@Test
	public void testPopulateODocumentWithCore() throws Exception {
		MockEntity mockEntity = new MockEntity();
		ODocument document = mockOrientDbRepository.convertToDocument(mockEntity);

		// run without periods set
		mockOrientDbRepository.populateODocumentWithCore(document, mockEntity);

		assertEquals(new Long(Long.MIN_VALUE), document.field("minJD"));
		assertEquals(new Long(Long.MAX_VALUE), document.field("maxJD"));
		assertNull(document.field("minEntry"));
		assertNull(document.field("maxEntry"));
		assertNull(document.field("minEntryCalendar"));
		assertNull(document.field("maxEntryCalendar"));

		List<IPeriod> list = new LinkedList<>();

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
		period4.setCreated(1L);
		period4.setModified(2L);
		list.add(period4);

		IPeriod period5 = new Period();
		period5.setFromEntry("5.1.901");
		period5.setToEntry(null);
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
		mockEntity.setPeriods(list);

		// run without periods set
		document = mockOrientDbRepository.convertToDocument(mockEntity);
		mockOrientDbRepository.populateODocumentWithCore(document, mockEntity);

		assertEquals(period5.getFromJD(), document.field("minJD"));
		assertEquals(period2.getToJD(), document.field("maxJD"));
		assertEquals(period5.getFromEntry(), document.field("minEntry"));
		assertEquals(period2.getToEntry(), document.field("maxEntry"));
		assertEquals(period5.getFromEntryCalendar(), document.field("minEntryCalendar"));
		assertEquals(period2.getToEntryCalendar(), document.field("maxEntryCalendar"));
	}

	@Test
	public void testPopulateEntityWithCore() throws Exception {
		// create mock entity
		ODocument document = new ODocument("Mock").field("created", 1L).field("modified", 2L);
		document.save();

		ODocument location = new ODocument("Location")
				.field("longitude", 123.0).field("latitude", -23.0)
				.field("parent", document).field("created", 1L).field("modified", 2L);
		location.save();

		ODocument period = new ODocument("Period")
				.field("fromEntryCalendar", "G").field("toEntryCalendar", "G")
				.field("fromEntry", "1.1585").field("toEntry", "2.1585")
				.field("from", 2299970L).field("to", 2300028L)
				.field("type", "period")
				.field("parent", document).field("created", 1L).field("modified", 2L);
		period.save();

		// do conversion
		MockEntity mockEntity = mockOrientDbRepository.convertToEntity(document);
		// populate!
		mockOrientDbRepository.populateEntityWithCore(document, mockEntity);

		// test for locations
		List<ILocation> list = mockOrientDbRepository.lazyLoadLocations(mockEntity);
		assertFalse(list.isEmpty());
		assertEquals(location.getIdentity().toString(), list.get(0).getId());

		// test for periods
		List<IPeriod> list2 = mockOrientDbRepository.lazyLoadPeriods(mockEntity);
		assertFalse(list2.isEmpty());
		assertEquals(period.getIdentity().toString(), list2.get(0).getId());
	}

	@Test
	public void testLazyLoadLocations() throws Exception {
		// create mock entity
		MockEntity mockEntity = new MockEntity();
		mockOrientDbRepository.save(mockEntity);

		List<ILocation> list = mockOrientDbRepository.lazyLoadLocations(mockEntity);
		assertTrue(list.isEmpty());

		ODocument document = new ODocument("Location")
				.field("longitude", 123.0).field("latitude", -23.0)
				.field("parent", new ORecordId(mockEntity.getId())).field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		list = mockOrientDbRepository.lazyLoadLocations(mockEntity);
		assertFalse(list.isEmpty());
		assertEquals(document.getIdentity().toString(), list.get(0).getId());
	}

	@Test
	public void testLazyLoadPeriods() throws Exception {
		// create mock entity
		MockEntity mockEntity = new MockEntity();
		mockOrientDbRepository.save(mockEntity);

		List<IPeriod> list = mockOrientDbRepository.lazyLoadPeriods(mockEntity);
		assertTrue(list.isEmpty());
		ODocument document = new ODocument("Period")
				.field("fromEntryCalendar", "G").field("toEntryCalendar", "G")
				.field("fromEntry", "1.1585").field("toEntry", "2.1585")
				.field("from", 2299970L).field("to", 2300028L)
				.field("type", "period")
				.field("parent", new ORecordId(mockEntity.getId())).field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		list = mockOrientDbRepository.lazyLoadPeriods(mockEntity);
		assertFalse(list.isEmpty());
		assertEquals(document.getIdentity().toString(), list.get(0).getId());
	}

	@Test
	public void testDelete() throws Exception {
		LocationRepository locationRepository = factory.produceRepository(OrientDbLocationRepository.class);
		PeriodRepository periodRepository = factory.produceRepository(OrientDbPeriodRepository.class);
		NodeRepository nodeRepository = factory.produceRepository(OrientDbNodeRepository.class);

		if (locationRepository == null || periodRepository == null) fail();

		factory.getDb().command(new OCommandSQL("truncate class Location")).execute();
		factory.getDb().command(new OCommandSQL("truncate class Period")).execute();

		INode node = new Node();
		node.setAlternativeTitles("Title");
		nodeRepository.save(node);

		// create locations and periods
		ILocation location = new Location();
		location.setParentModel("Node");
		location.setParentId(node.getId());
		location.setLatitude(10.0);
		location.setLongitude(5.0);
		assertTrue(locationRepository.save(location));

		IPeriod period1 = new Period();
		period1.setParentModel("Node");
		period1.setParentId(node.getId());
		period1.setFromEntry("1502");
		period1.setToEntry("1516");
		assertTrue(periodRepository.save(period1));

		IPeriod period2 = new Period();
		period2.setParentModel("Node");
		period2.setParentId(node.getId());
		period2.setToEntry("1576");
		assertTrue(periodRepository.save(period2));

		assertEquals(1, locationRepository.count());
		assertEquals(2, periodRepository.count());

		// test cascaded deletion of connected locations and periods
		nodeRepository.delete(node);

		assertEquals(0, nodeRepository.count());
		assertEquals(0, locationRepository.count());
		assertEquals(0, periodRepository.count());
	}

	/**
	 * Mock entity
	 */
	private class MockEntity extends AbstractCoreModel implements SegradaCoreEntity {
		private String id;

		@Override
		public void setId(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getTitle() {
			return "DUMMY";
		}
	}

	/**
	 * Partial/mock repository to test methods
	 */
	private class MockOrientDbRepository extends AbstractCoreOrientDbRepository<MockEntity> {
		public MockOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
			super(repositoryFactory);
		}

		@Override
		public MockEntity convertToEntity(ODocument document) {
			MockEntity entity = new MockEntity();
			populateEntityWithBaseData(document, entity);
			return entity;
		}

		@Override
		public ODocument convertToDocument(MockEntity entity) {
			return createOrLoadDocument(entity);
		}

		@Override
		public String getModelClassName() {
			return "Mock";
		}
	}
}