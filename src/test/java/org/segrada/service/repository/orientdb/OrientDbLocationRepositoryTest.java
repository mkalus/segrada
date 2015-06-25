package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Location;
import org.segrada.model.prototype.ILocation;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrientDbLocationRepositoryTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * repository to test
	 */
	private OrientDbLocationRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		OrientDbRepositoryFactory factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbLocationRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		repository.getDb().command(new OCommandSQL("truncate class Location")).execute();
		repository.getDb().command(new OCommandSQL("delete vertex V")).execute();

		// close db
		try {
			repository.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("Location", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument parent = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent.save();

		ODocument document = new ODocument("Location")
				.field("longitude", 123.0).field("latitude", -23.0)
				.field("parent", parent).field("created", 1L).field("modified", 2L);
		// persist to database to create id
		document.save();

		ILocation location = repository.convertToEntity(document);

		assertEquals(new Double(123.0), location.getLongitude());
		assertEquals(new Double(-23.0), location.getLatitude());
		assertEquals(new Long(1L), location.getCreated());
		assertEquals(new Long(2L), location.getModified());
		assertEquals(parent.getIdentity().toString(), location.getParentId());
		assertEquals("Node", location.getParentModel());
		assertEquals(document.getIdentity().toString(), location.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		ODocument parent = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent.save();

		ILocation location = new Location();
		location.setLatitude(1.0);
		location.setLongitude(-1.0);
		location.setCreated(1L);
		location.setModified(2L);
		location.setParentId(parent.getIdentity().toString());
		location.setParentModel("Node");

		// first without id
		ODocument document = repository.convertToDocument(location);
		assertEquals(new Double(1), document.field("latitude"));
		assertEquals(new Double(-1), document.field("longitude"));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));
		assertEquals(parent.getIdentity().toString(), document.field("parent", String.class));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		location.setId(id);

		ODocument newDocument = repository.convertToDocument(location);

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

		ILocation location1 = new Location();
		location1.setLatitude(1.0);
		location1.setLongitude(-1.0);
		location1.setCreated(1L);
		location1.setModified(2L);
		location1.setParentId(id1);
		repository.save(location1);

		ILocation location2 = new Location();
		location2.setLatitude(10.0);
		location2.setLongitude(-10.0);
		location2.setCreated(1L);
		location2.setModified(2L);
		location2.setParentId(id1);
		repository.save(location2);

		ILocation location3 = new Location();
		location3.setLatitude(50.0);
		location3.setLongitude(50.0);
		location3.setCreated(1L);
		location3.setModified(2L);
		location3.setParentId(id1);
		repository.save(location3);

		List<ILocation> locations = repository.findByParent(id1);

		assertTrue(locations.size() == 3);

		locations = repository.findByParent(id2);
		assertTrue(locations.size() == 0);
	}

	@Test
	public void testFindClosest() throws Exception {
		ODocument parent1 = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent1.save();
		ODocument parent2 = new ODocument("Node").field("title", "ref2").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent2.save();

		String id1 = parent1.getIdentity().toString();
		String id2 = parent2.getIdentity().toString();

		ILocation location1 = new Location();
		location1.setLatitude(1.0);
		location1.setLongitude(-1.0);
		location1.setCreated(1L);
		location1.setModified(2L);
		location1.setParentId(id1);
		repository.save(location1);

		ILocation location2 = new Location();
		location2.setLatitude(10.0);
		location2.setLongitude(-10.0);
		location2.setCreated(1L);
		location2.setModified(2L);
		location2.setParentId(id1);
		repository.save(location2);

		ILocation location3 = new Location();
		location3.setLatitude(50.0);
		location3.setLongitude(50.0);
		location3.setCreated(1L);
		location3.setModified(2L);
		location3.setParentId(id1);
		repository.save(location3);

		ILocation location4 = new Location();
		location4.setLatitude(1.0);
		location4.setLongitude(-1.0);
		location4.setCreated(1L);
		location4.setModified(2L);
		location4.setParentId(id2);
		repository.save(location4);

		List<ILocation> locations = repository.findClosest(0.0, 0.0, 1000.0);

		assertTrue(locations.size() == 2);
		assertTrue(Objects.equals(locations.get(0).getDistance(), locations.get(1).getDistance()));
		assertTrue(Math.floor(locations.get(0).getDistance()) == 157.0);
		assertTrue(Math.floor(locations.get(1).getDistance()) == 157.0);
	}

	@Test
	public void testFindNear() throws Exception {
		ODocument parent1 = new ODocument("Node").field("title", "ref1").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent1.save();
		ODocument parent2 = new ODocument("Node").field("title", "ref2").field("description", "desc")
				.field("descriptionMarkup", "default").field("created", 1L).field("modified", 2L);
		parent2.save();

		String id1 = parent1.getIdentity().toString();
		String id2 = parent2.getIdentity().toString();

		ILocation location1 = new Location();
		location1.setLatitude(1.0);
		location1.setLongitude(-1.0);
		location1.setCreated(1L);
		location1.setModified(2L);
		location1.setParentId(id1);
		repository.save(location1);

		ILocation location2 = new Location();
		location2.setLatitude(10.0);
		location2.setLongitude(-10.0);
		location2.setCreated(1L);
		location2.setModified(2L);
		location2.setParentId(id1);
		repository.save(location2);

		ILocation location3 = new Location();
		location3.setLatitude(5.0);
		location3.setLongitude(5.0);
		location3.setCreated(1L);
		location3.setModified(2L);
		location3.setParentId(id1);
		repository.save(location3);

		ILocation location4 = new Location();
		location4.setLatitude(1.0);
		location4.setLongitude(-1.0);
		location4.setCreated(1L);
		location4.setModified(2L);
		location4.setParentId(id2);
		repository.save(location4);

		List<ILocation> locations = repository.findNear(0.0, 0.0, 1000.0);

		assertTrue(locations.size() == 3);
		assertTrue(Math.floor(locations.get(0).getDistance()) == 157.0);
		assertTrue(Math.floor(locations.get(1).getDistance()) == 157.0);
		assertTrue(Math.floor(locations.get(2).getDistance()) == 786.0);
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

		ILocation location1 = new Location();
		location1.setLatitude(1.0);
		location1.setLongitude(-1.0);
		location1.setCreated(1L);
		location1.setModified(2L);
		location1.setParentId(id1);
		repository.save(location1);

		ILocation location2 = new Location();
		location2.setLatitude(10.0);
		location2.setLongitude(-10.0);
		location2.setCreated(1L);
		location2.setModified(2L);
		location2.setParentId(id1);
		repository.save(location2);

		ILocation location3 = new Location();
		location3.setLatitude(50.0);
		location3.setLongitude(50.0);
		location3.setCreated(1L);
		location3.setModified(2L);
		location3.setParentId(id1);
		repository.save(location3);

		ILocation location4 = new Location();
		location4.setLatitude(1.0);
		location4.setLongitude(-1.0);
		location4.setCreated(1L);
		location4.setModified(2L);
		location4.setParentId(id2);
		repository.save(location4);

		List<ILocation> locations = repository.findWithin(-1, -10, 10, 10);
		assertTrue(locations.size() == 3);

		locations = repository.findWithin(10, 10, -1, -10);
		assertTrue(locations.size() == 3);
	}
}