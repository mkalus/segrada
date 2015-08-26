package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Node;
import org.segrada.model.Relation;
import org.segrada.model.RelationType;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.RelationTypeRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

import static org.junit.Assert.*;

public class OrientDbRelationRepositoryTest {
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
	private OrientDbRelationRepository repository;

	@Before
	public void setUp() throws Exception {
		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbRelationRepository.class);
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
		assertEquals("Relation", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-2")
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

		// now load from db and convert
		query = new OSQLSynchQuery<>("select * from " + relationO.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertFalse(result.isEmpty());

		// get first entity
		ODocument document = result.get(0);

		// convert to relation
		IRelation relation = repository.convertToEntity(document);
		assertNotNull(relation);

		assertEquals("rel_description", relation.getDescription());
		assertEquals("default", relation.getDescriptionMarkup());
		assertEquals(new Integer(0x123456), relation.getColor());
		assertEquals(new Long(1L), relation.getCreated());
		assertEquals(new Long(2L), relation.getModified());
		assertEquals(document.getIdentity().toString(), relation.getId());

		assertEquals(relationType.getIdentity().toString(), relation.getRelationType().getId());
		assertEquals(node1.getIdentity().toString(), relation.getFromEntity().getId());
		assertEquals(node2.getIdentity().toString(), relation.getToEntity().getId());
	}

	@Test(expected = NotImplementedException.class)
	public void testConvertToDocument() throws Exception {
		repository.convertToDocument(new Relation());
	}

	@Test
	public void testReallyConvertToDocument() throws Exception {
		IRelationType relationType = new RelationType();

		IRelation relation = new Relation();
		relation.setRelationType(relationType);
		relation.setDescription("Description");
		relation.setDescriptionMarkup("default");
		relation.setColor(0x123456);
		relation.setCreated(1L);
		relation.setModified(2L);

		// first without id
		ODocument document = repository.reallyConvertToDocument(relation);

		assertNull(document.field("relationType"));
		assertEquals("Description", document.field("description"));
		assertEquals("default", document.field("descriptionMarkup"));
		assertEquals(new Integer(0x123456), document.field("color", Integer.class));
		assertEquals(new Long(1L), document.field("created", Long.class));
		assertEquals(new Long(2L), document.field("modified", Long.class));

		// also save type check again
		// now create an entity
		ODocument relationTypeO = new ODocument("RelationType")
				.field("fromTitle", "fromTitle").field("toTitle", "toTitle")
				.field("fromTitleAsc", "fromTitle").field("toTitleAsc", "toTitle")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();
		relationType.setId(relationTypeO.getIdentity().toString());

		document = repository.reallyConvertToDocument(relation);

		assertEquals(relationTypeO.getIdentity(), document.field("relationType", ORecord.class));

		// add more data to save record
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-2")
				.field("alternativeTitles", "alternativeTitles")
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

		document.field("relationLink", relationLink);

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		relation.setId(id);

		ODocument newDocument = repository.reallyConvertToDocument(relation);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testFindByRelation() throws Exception {
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-2")
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

		// find by from
		INode node = new Node();
		node.setId(node1.getIdentity().toString());

		List<IRelation> list = repository.findByRelation(node);
		assertEquals(1, list.size());
		assertEquals(relationO.getIdentity().toString(), list.get(0).getId());
		assertEquals(node1.getIdentity().toString(), list.get(0).getFromEntity().getId());
		assertEquals(node2.getIdentity().toString(), list.get(0).getToEntity().getId());

		// find by to
		node = new Node();
		node.setId(node2.getIdentity().toString());

		list = repository.findByRelation(node);
		assertEquals(1, list.size());
		assertEquals(relationO.getIdentity().toString(), list.get(0).getId());
		assertEquals(node1.getIdentity().toString(), list.get(0).getFromEntity().getId());
		assertEquals(node2.getIdentity().toString(), list.get(0).getToEntity().getId());
	}

	@Test
	public void testFindByRelationType() throws Exception {
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-2")
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

		IRelationType relationType1 = new RelationType();
		relationType1.setId(relationType.getIdentity().toString());

		List<IRelation> list = repository.findByRelationType(relationType1);
		assertEquals(1, list.size());
		assertEquals(relationO.getIdentity().toString(), list.get(0).getId());
		assertEquals(node1.getIdentity().toString(), list.get(0).getFromEntity().getId());
		assertEquals(node2.getIdentity().toString(), list.get(0).getToEntity().getId());
	}

	@Test
	public void testDeleteByRelation() throws Exception {
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-2")
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

		// create node
		INode node = new Node();
		node.setId(node1.getIdentity().toString());

		// delete by node
		repository.deleteByRelation(node);

		// try to find relation
		query = new OSQLSynchQuery<>("select * from " + relationO.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());

		// try to find relation link
		query = new OSQLSynchQuery<>("select * from " + relationLink.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());
	}

	@Test
	public void testDeleteByRelationType() throws Exception {
		ODocument node1 = new ODocument("Node").field("title", "title 1").field("titleasc", "title-1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L).save();

		ODocument node2 = new ODocument("Node").field("title", "title 2").field("titleasc", "title-2")
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

		// create relation type
		IRelationType relationType1 = new RelationType();
		relationType1.setId(relationType.getIdentity().toString());

		// delete by node
		repository.deleteByRelationType(relationType1);

		// try to find relation
		query = new OSQLSynchQuery<>("select * from " + relationO.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());

		// try to find relation link
		query = new OSQLSynchQuery<>("select * from " + relationLink.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertTrue(result.isEmpty());
	}

	@Test
	public void testPaginate() throws Exception {
		//fail("Test not implemented yet.");
		//TODO: do later
	}

	@Test
	public void testSave() throws Exception {
		NodeRepository nodeRepository = factory.produceRepository(OrientDbNodeRepository.class);
		RelationTypeRepository relationTypeRepository = factory.produceRepository(OrientDbRelationTypeRepository.class);
		assertNotNull(nodeRepository);
		assertNotNull(relationTypeRepository);

		// create nodes and relation type
		INode node1 = new Node();
		node1.setTitle("Node 1");
		assertTrue(nodeRepository.save(node1));
		INode node2 = new Node();
		node2.setTitle("Node 2");
		assertTrue(nodeRepository.save(node2));
		IRelationType relationType = new RelationType();
		relationType.setFromTitle("from");
		relationType.setToTitle("to");
		assertTrue(relationTypeRepository.save(relationType));

		// create relation
		IRelation relation = new Relation();
		relation.setFromEntity(node1);
		relation.setToEntity(node2);
		relation.setRelationType(relationType);

		// do save
		assertTrue(repository.save(relation));
		assertNotNull(relation.getId());

		// check for the edge
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from IsRelation where out = ? AND in = ?");
		List<ODocument> result = factory.getDb().command(query).execute(new ORecordId(node1.getId()), new ORecordId(node2.getId()));
		assertFalse(result.isEmpty());
		ODocument relationLink = result.get(0);
		String id = relation.getId();

		// update the same relation again
		assertTrue(repository.save(relation));
		assertEquals(id, relation.getId());

		// check for the edge again, should be the same id
		query = new OSQLSynchQuery<>("select * from IsRelation where out = ? AND in = ?");
		result = factory.getDb().command(query).execute(new ORecordId(node1.getId()), new ORecordId(node2.getId()));
		assertEquals(1, result.size());
		ODocument relationLinkTest = result.get(0);

		assertEquals(relationLink.getIdentity().toString(), relationLinkTest.getIdentity().toString());

		// change to/from and save again
		relation.setFromEntity(node2);
		relation.setToEntity(node1);

		// do save
		assertTrue(repository.save(relation));

		// check for the edge again, should be the same id
		query = new OSQLSynchQuery<>("select * from IsRelation where in = ? AND out = ?");
		result = factory.getDb().command(query).execute(new ORecordId(node1.getId()), new ORecordId(node2.getId()));
		assertEquals(1, result.size());
		relationLinkTest = result.get(0);

		assertNotEquals(relationLink.getIdentity().toString(), relationLinkTest.getIdentity().toString());

		// old relation should not exist any more
		query = new OSQLSynchQuery<>("select * from " + relationLink.getIdentity().toString());
		result = factory.getDb().command(query).execute();
		assertEquals(0, result.size());
	}
}