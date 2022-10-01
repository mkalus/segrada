package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.segrada.model.RelationType;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrientDbRelationTypeRepositoryTest {

	//TODO: test from/to tags, tags themselves

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
	private OrientDbRelationTypeRepository repository;

	@BeforeEach
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbRelationTypeRepository.class);
	}

	@AfterEach
	public void tearDown() throws Exception {
		// truncate db
		factory.getDb().command(new OCommandSQL("delete vertex RelationType")).execute();
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
		assertEquals("RelationType", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		//TODO: test pictogram, tags

		// now create an entity
		ODocument document = new ODocument("RelationType")
				.field("fromTitle", "fromTitle").field("toTitle", "toTitle")
				.field("fromTitleAsc", "fromTitle").field("toTitleAsc", "toTitle")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L);

		// persist to database to create id
		document.save();

		IRelationType relationType = repository.convertToEntity(document);

		assertEquals("fromTitle", relationType.getFromTitle());
		assertEquals("toTitle", relationType.getToTitle());
		assertEquals("Description", relationType.getDescription());
		assertEquals("default", relationType.getDescriptionMarkup());
		assertEquals(new Integer(0x123456), relationType.getColor());
		assertEquals(new Long(1L), relationType.getCreated());
		assertEquals(new Long(2L), relationType.getModified());
		assertEquals(document.getIdentity().toString(), relationType.getId());
	}

	@Test
	public void testConvertToDocument() throws Exception {
		//TODO: test pictogram, tags

		IRelationType relationType = new RelationType();
		relationType.setFromTitle("fromTitle");
		relationType.setToTitle("toTitle");
		relationType.setDescription("Description");
		relationType.setDescriptionMarkup("default");
		relationType.setColor(0x123456);
		relationType.setCreated(1L);
		relationType.setModified(2L);

		// first without id
		ODocument document = repository.convertToDocument(relationType);

		assertEquals("fromTitle", document.field("fromTitle"));
		assertEquals("toTitle", document.field("toTitle"));
		assertEquals("Description", document.field("description"));
		assertEquals("default", document.field("descriptionMarkup"));
		assertEquals(new Integer(0x123456), document.field("color", Integer.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		relationType.setId(id);

		ODocument newDocument = repository.convertToDocument(relationType);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY fromTitleAsc ASC, toTitleAsc ASC", repository.getDefaultOrder(true));
		assertEquals(" fromTitleAsc ASC, toTitleAsc ASC", repository.getDefaultOrder(false));
	}

	@Test
	public void testFindBySearchTerm() throws Exception {
		IRelationType relationType = new RelationType();
		relationType.setFromTitle("fromTitle here");
		relationType.setToTitle("toTitle there");
		relationType.setDescription("Description");
		relationType.setDescriptionMarkup("default");
		relationType.setColor(0x123456);
		relationType.setCreated(1L);
		relationType.setModified(2L);

		repository.save(relationType);

		// empty term
		List<IRelationType> hits = repository.findBySearchTerm("", 1, true);
		assertEquals(1, hits.size());

		// unknown term
		hits = repository.findBySearchTerm("complexxxxxx", 1, true);
		assertEquals(0, hits.size());

		// 1 term
		hits = repository.findBySearchTerm("fromTitle", 1, true);
		assertEquals(1, hits.size());

		// to  title
		hits = repository.findBySearchTerm("toTitle", 1, true);
		assertEquals(1, hits.size());

		// 2 terms
		hits = repository.findBySearchTerm("here toTitle", 1, true);
		assertEquals(1, hits.size());

		// partial terms
		hits = repository.findBySearchTerm("fro tot", 1, true);
		assertEquals(1, hits.size());
	}

	@Test
	public void testPaginate() throws Exception {
		//fail("Test not implemented yet.");
		//TODO: do later
	}

	@Test
	public void testDelete() throws Exception {
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		// now create an entity
		ODocument relationType = new ODocument("RelationType")
				.field("fromTitle", "fromTitle").field("toTitle", "toTitle")
				.field("fromTitleAsc", "fromTitle").field("toTitleAsc", "toTitle")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		// create link
		factory.getDb().command(new OCommandSQL("create edge IsRelation from " + node1.getIdentity().toString() + " to " + node2.getIdentity().toString())).execute();
		// find link
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from IsRelation where out = ? AND in = ?");
		List<ODocument> result = factory.getDb().command(query).execute(node1.getIdentity(), node2.getIdentity());
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

		// load relation type
		IRelationType relationType1 = repository.find(relationType.getIdentity().toString());

		// delete it
		repository.delete(relationType1);

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
