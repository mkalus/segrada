package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Location;
import org.segrada.model.Node;
import org.segrada.model.Period;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.ITag;
import org.segrada.service.repository.LocationRepository;
import org.segrada.service.repository.PeriodRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OrientDbNodeRepositoryTest {
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
	private OrientDbNodeRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbNodeRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("delete vertex V")).execute();
		factory.getDb().command(new OCommandSQL("delete edge E")).execute();
		factory.getDb().command(new OCommandSQL("truncate class RelationType")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testGetModelClassName() throws Exception {
		assertEquals("Node", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		//TODO: test pictogram, tags

		// now create an entity
		ODocument document = new ODocument("Node").field("title", "title")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L);
		// persist to database to create id
		document.save();

		INode node = repository.convertToEntity(document);

		assertEquals("title", node.getTitle());
		assertEquals("alternativeTitles", node.getAlternativeTitles());
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

		INode node = new Node();
		node.setTitle("title");
		node.setAlternativeTitles("alternativeTitles");
		node.setDescription("Description");
		node.setDescriptionMarkup("default");
		node.setColor(0x123456);
		node.setCreated(1L);
		node.setModified(2L);

		// first without id
		ODocument document = repository.convertToDocument(node);

		assertEquals("title", document.field("title"));
		assertEquals("alternativeTitles", document.field("alternativeTitles"));
		assertEquals("Description", document.field("description"));
		assertEquals("default", document.field("descriptionMarkup"));
		assertEquals(new Integer(0x123456), document.field("color", Integer.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		node.setId(id);

		ODocument newDocument = repository.convertToDocument(node);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY title", repository.getDefaultOrder(true));
		assertEquals(" title", repository.getDefaultOrder(false));
	}

	@Test
	public void testFindBySearchTerm() throws Exception {
		INode node = new Node();
		node.setTitle("This is the title");
		node.setAlternativeTitles("and this is its alternatives");
		node.setDescription("Description");
		node.setDescriptionMarkup("default");
		node.setColor(0x123456);
		node.setCreated(1L);
		node.setModified(2L);

		repository.save(node);

		// empty term
		List<INode> hits = repository.findBySearchTerm("", 1, true);
		assertEquals(1, hits.size());

		// unknown term
		hits = repository.findBySearchTerm("complexxxxxx", 1, true);
		assertEquals(0, hits.size());

		// 1 term
		hits = repository.findBySearchTerm("title", 1, true);
		assertEquals(1, hits.size());

		// alternative title
		hits = repository.findBySearchTerm("alternatives", 1, true);
		assertEquals(1, hits.size());

		// 2 terms
		hits = repository.findBySearchTerm("title alternatives", 1, true);
		assertEquals(1, hits.size());

		// partial terms
		hits = repository.findBySearchTerm("alt tit", 1, true);
		assertEquals(1, hits.size());
	}

	@Test
	public void testPaginate() throws Exception {
		TagRepository tagRepository = factory.produceRepository(OrientDbTagRepository.class);

		if (tagRepository == null) fail();

		// create a few tags, too
		ITag[] tags = new ITag[10];
		for (int i = 0; i < 10; i++) {
			ITag tag = new Tag();
			tag.setTitle("Tag " + (i+1));
			tagRepository.save(tag);
			tags[i] = tag;
		}

		// create the nodes
		for (int i = 1; i <= 33; i++) {
			INode node = new Node();
			node.setAlternativeTitles("Title " + i);
			node.setTags(new String[] { "Tag " + (i % 10 + 1) });
			repository.save(node);
		}

		// first simple tests of pagination
		PaginationInfo<INode> pi = repository.paginate(1, 10, null);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 4);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 10);

		// last page
		pi = repository.paginate(4, 10, null);
		assertTrue(pi.page == 4);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 4);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 3);

		// different page size
		pi = repository.paginate(4, 5, null);
		assertTrue(pi.page == 4);
		assertTrue(pi.entriesPerPage == 5);
		assertTrue(pi.pages == 7);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 5);

		// do incorrect pagination 1
		pi = repository.paginate(0, 10, null);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 4);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 10);

		// do incorrect pagination 2
		pi = repository.paginate(1, 0, null);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 4);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 10);

		// out of bounds
		pi = repository.paginate(10, 10, null);
		assertTrue(pi.page == 4);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 4);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 3);

		// full text filter
		Map<String, Object> filters = new HashMap<>();
		filters.put("search", "title");
		pi = repository.paginate(1, 10, filters);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 4);
		assertTrue(pi.total == 33);
		assertTrue(pi.entities.size() == 10);

		// empty result
		filters.put("search", "alt");
		pi = repository.paginate(1, 10, filters);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 1);
		assertTrue(pi.total == 0);
		assertTrue(pi.entities.size() == 0);

		// filter by tags
		filters.clear();
		filters.put("tags", new String[] { "Tag 1",  "Tag 2" });
		pi = repository.paginate(1, 10, filters);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 1);
		assertTrue(pi.total == 7);
		assertTrue(pi.entities.size() == 7);

		// filter by tags and full text
		filters.clear();
		filters.put("tags", new String[] { "Tag 1",  "Tag 2" });
		filters.put("search", "title");
		pi = repository.paginate(1, 10, filters);
		assertTrue(pi.page == 1);
		assertTrue(pi.entriesPerPage == 10);
		assertTrue(pi.pages == 1);
		assertTrue(pi.total == 7);
		assertTrue(pi.entities.size() == 7);

		//TODO more filters
	}

	@Test
	public void testDelete() throws Exception {
		INode node = new Node();
		node.setTitle("This is the title");
		node.setAlternativeTitles("and this is its alternatives");
		node.setDescription("Description");
		node.setDescriptionMarkup("default");
		node.setColor(0x123456);
		node.setCreated(1L);
		node.setModified(2L);

		repository.save(node);

		// connect nodes
		ODocument node2 = new ODocument("Node").field("title", "title 2")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		// now create an entity
		ODocument relationType = new ODocument("RelationType")
				.field("fromTitle", "fromTitle").field("toTitle", "toTitle")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		// create link
		factory.getDb().command(new OCommandSQL("create edge IsRelation from " + node.getId() + " to " + node2.getIdentity().toString())).execute();
		// find link
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from IsRelation where out = ? AND in = ?");
		List<ODocument> result = factory.getDb().command(query).execute(new ORecordId(node.getId()), node2.getIdentity());
		assertFalse(result.isEmpty());
		ODocument relationLink = result.get(0);

		ODocument relationO = new ODocument("Relation")
				.field("relationType", relationType)
				.field("relationLink", relationLink)
				.field("description", "rel_description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		repository.delete(node);

		// try to find relation
		query = new OSQLSynchQuery<>("select * from " + relationO.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());

		// try to find relation link
		query = new OSQLSynchQuery<>("select * from " + relationLink.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());
	}
}