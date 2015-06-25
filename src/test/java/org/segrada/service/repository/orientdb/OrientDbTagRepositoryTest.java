package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Node;
import org.segrada.model.Tag;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import java.util.List;

import static org.junit.Assert.*;

public class OrientDbTagRepositoryTest {
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
	private OrientDbTagRepository repository;

	@Before
	public void setUp() throws Exception {
		//TODO: rework tag repository to work better

		// set up schema if needed
		//orientDBTestInstance.dropDatabase();
		//TODO: make dropDatabase work nicer - this might be a bug in OrientDB
		orientDBTestInstance.setUpSchemaIfNeeded();

		// truncate db
		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create repository
		repository =  factory.produceRepository(OrientDbTagRepository.class);
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
		assertEquals("Tag", repository.getModelClassName());
	}

	@Test
	public void testConvertToEntity() throws Exception {
		ODocument document = new ODocument("Tag").field("title", "title").field("created", 1L).field("modified", 2L);;
		document.save();

		ITag tag = repository.convertToEntity(document);

		assertEquals("title", tag.getTitle());
		assertEquals(new Long(1L), tag.getCreated());
		assertEquals(new Long(2L), tag.getModified());
		assertEquals(document.getIdentity().toString(), tag.getId());

		document.delete();
	}

	@Test
	public void testConvertToDocument() throws Exception {
		ITag tag = new Tag();
		tag.setTitle("title");

		// first without id
		ODocument document = repository.convertToDocument(tag);

		assertEquals("title", document.field("title"));

		// class name should be correct
		assertEquals("Tag", document.getClassName());

		// save document to get id
		document.save();
		String id = document.getIdentity().toString();

		// set id and test conversion
		tag.setId(id);

		ODocument newDocument = repository.convertToDocument(tag);

		assertEquals(document.getIdentity().toString(), newDocument.getIdentity().toString());
	}

	@Test
	public void testGetDefaultOrder() throws Exception {
		assertEquals(" ORDER BY title", repository.getDefaultOrder(true));
		assertEquals(" title", repository.getDefaultOrder(false));
	}

	@Test
	public void testFindByTitle() throws Exception {
		ITag tag = new Tag();
		tag.setTitle("title XYZ");

		repository.save(tag);

		ITag testTag = repository.findByTitle(tag.getTitle());

		assertEquals(tag.getId(), testTag.getId());

		// return null for non-existing entities
		testTag = repository.findByTitle("NON-EXIST");

		assertEquals(null, testTag);
	}

	@Test
	public void testFindBySearchTerm() throws Exception {
		ITag tag = new Tag();
		tag.setTitle("This is a complex title with an Ümlaut");

		repository.save(tag);

		// empty term
		List<ITag> hits = repository.findBySearchTerm("", 1, true);
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
	public void testCreateNewTagsByTitles() throws Exception {
		// make sure we have not created any tags yet
		assertTrue("Repository not empty", repository.count() == 0);

		// some tags
		String[] tags = new String[]{"Tag1", "Tag2", "Tag3"};

		repository.createNewTagsByTitles(tags);

		// correctly created all tags?
		assertTrue(repository.count() == 3);
		assertNotNull(repository.findByTitle("Tag1"));
		assertNotNull(repository.findByTitle("Tag2"));
		assertNotNull(repository.findByTitle("Tag3"));

		// now try to create new entries, but list contains entries already created
		tags = new String[]{"Tag3", "Tag4"};

		repository.createNewTagsByTitles(tags);

		// correctly created one tag?
		assertTrue(repository.count() == 4);
		assertNotNull(repository.findByTitle("Tag1"));
		assertNotNull(repository.findByTitle("Tag2"));
		assertNotNull(repository.findByTitle("Tag3"));
		assertNotNull(repository.findByTitle("Tag4"));
	}

	@Test
	public void testFindTagsByTitles() throws Exception {
		// make sure we have not created any tags yet
		assertTrue("Repository not empty", repository.count() == 0);

		// create some tags
		for (int i = 1; i <= 10; i++) {
			ITag tag = new Tag();
			tag.setTitle("Tag" + i);
			repository.save(tag);
		}

		// variable
		String[] tags;

		// test null and empty list
		assertEquals(0, repository.findTagsByTitles(null).size());
		assertEquals(0, repository.findTagsByTitles(new String[]{}).size());

		// now test known tags
		tags = new String[]{"Tag1", "Tag2", "Tag3"};
		assertEquals(3, repository.findTagsByTitles(tags).size());

		// now test with unknown tags
		tags = new String[]{"Tag1", "Tag2", "Tag99"};
		assertEquals(2, repository.findTagsByTitles(tags).size());

		// now test only unknown tags
		tags = new String[]{"Tag98", "Tag99"};
		assertEquals(0, repository.findTagsByTitles(tags).size());
	}

	@Test
	public void testFindTagIdsByTitles() throws Exception {
		// make sure we have not created any tags yet
		assertTrue("Repository not empty", repository.count() == 0);

		// create some tags
		String[] ids = new String[10];
		for (int i = 1; i <= 10; i++) {
			ITag tag = new Tag();
			tag.setTitle("Tag" + i);
			repository.save(tag);
			ids[i-1] = tag.getId();
		}

		// variable
		String[] tagIds;

		// test null and empty list
		assertEquals(0, repository.findTagIdsByTitles(null).length);
		assertEquals(0, repository.findTagIdsByTitles(new String[]{}).length);

		// now test known tags
		tagIds = new String[]{"Tag1", "Tag2", "Tag3"};
		assertEquals(3, repository.findTagIdsByTitles(tagIds).length);
		assertArrayEquals(new String[]{ids[0], ids[1], ids[2]}, repository.findTagIdsByTitles(tagIds));

		// now test with unknown tags
		tagIds = new String[]{"Tag1", "Tag2", "Tag99"};
		assertEquals(2, repository.findTagIdsByTitles(tagIds).length);
		assertArrayEquals(new String[]{ids[0], ids[1]}, repository.findTagIdsByTitles(tagIds));

		// now test only unknown tags
		tagIds = new String[]{"Tag98", "Tag99"};
		assertEquals(0, repository.findTagIdsByTitles(tagIds).length);
	}

	@Test
	public void testFindTagTitlesByIds() throws Exception {
		// make sure we have not created any tags yet
		assertTrue("Repository not empty", repository.count() == 0);

		// create some tags
		String[] ids = new String[10];
		for (int i = 1; i <= 10; i++) {
			ITag tag = new Tag();
			tag.setTitle("Tag" + i);
			repository.save(tag);
			ids[i-1] = tag.getId();
		}

		// variable
		String[] tagIds;

		// test null and empty list
		assertEquals(0, repository.findTagTitlesByIds(null).length);
		assertEquals(0, repository.findTagTitlesByIds(new String[]{}).length);

		// now test known tags
		tagIds = new String[]{ids[0], ids[1], ids[2]};
		assertEquals(3, repository.findTagTitlesByIds(tagIds).length);
		assertArrayEquals(new String[]{"Tag1", "Tag2", "Tag3"}, repository.findTagTitlesByIds(tagIds));

		// now test with unknown tags
		tagIds = new String[]{ids[0], ids[1], "#99:99"};
		assertEquals(2, repository.findTagTitlesByIds(tagIds).length);
		assertArrayEquals(new String[]{"Tag1", "Tag2"}, repository.findTagTitlesByIds(tagIds));

		// now test only unknown tags
		tagIds = new String[]{"#99:89", "#99:99"};
		assertEquals(0, repository.findTagTitlesByIds(tagIds).length);
	}

	/*
	Essentially tested above
	@Test
	public void testFindTagDocumentsByCriteria() throws Exception {
		fail();
	}*/

	@Test
	public void testFindTagIdsByParent() throws Exception {
		ITag root = new Tag();
		root.setTitle("Root");
		repository.save(root);
		ITag subTag1 = new Tag();
		subTag1.setTitle("Sub 1");
		repository.save(subTag1);
		ITag subTag2 = new Tag();
		subTag2.setTitle("Sub 2");
		repository.save(subTag2);
		ITag sub2Tag1 = new Tag();
		sub2Tag1.setTitle("Sub 2 => Sub 1");
		repository.save(sub2Tag1);
		ITag sub2Tag2 = new Tag();
		sub2Tag2.setTitle("Sub 2 => Sub 2");
		repository.save(sub2Tag2);

		// add connections
		repository.connectTag(root, subTag1);
		repository.connectTag(root, subTag2);
		repository.connectTag(subTag2, sub2Tag1);
		repository.connectTag(subTag2, sub2Tag2);

		String[] tagIds;

		// find single tag
		tagIds = repository.findTagIdsByParent(subTag1.getId());
		assertEquals(1, tagIds.length);
		assertEquals(subTag1.getId(), tagIds[0]);

		// find subtags
		tagIds = repository.findTagIdsByParent(subTag2.getId());
		assertEquals(3, tagIds.length);
		assertArrayEquals(new String[]{subTag2.getId(),sub2Tag1.getId(),sub2Tag2.getId()}, tagIds);

		// find whole tree
		tagIds = repository.findTagIdsByParent(root.getId());
		assertEquals(5, tagIds.length);
	}

	@Test
	public void testFindTagIdsByParentTitle() throws Exception {
		ITag root = new Tag();
		root.setTitle("Root");
		repository.save(root);
		ITag subTag1 = new Tag();
		subTag1.setTitle("Sub 1");
		repository.save(subTag1);
		ITag subTag2 = new Tag();
		subTag2.setTitle("Sub 2");
		repository.save(subTag2);
		ITag sub2Tag1 = new Tag();
		sub2Tag1.setTitle("Sub 2 => Sub 1");
		repository.save(sub2Tag1);
		ITag sub2Tag2 = new Tag();
		sub2Tag2.setTitle("Sub 2 => Sub 2");
		repository.save(sub2Tag2);

		// add connections
		repository.connectTag(root, subTag1);
		repository.connectTag(root, subTag2);
		repository.connectTag(subTag2, sub2Tag1);
		repository.connectTag(subTag2, sub2Tag2);

		String[] tagIds;

		// find single tag
		tagIds = repository.findTagIdsByParentTitle(subTag1.getTitle());
		assertEquals(1, tagIds.length);
		assertEquals(subTag1.getId(), tagIds[0]);

		// find subtags
		tagIds = repository.findTagIdsByParentTitle(subTag2.getTitle());
		assertEquals(3, tagIds.length);
		assertArrayEquals(new String[]{subTag2.getId(),sub2Tag1.getId(),sub2Tag2.getId()}, tagIds);

		// find whole tree
		tagIds = repository.findTagIdsByParentTitle(root.getTitle());
		assertEquals(5, tagIds.length);
	}

	@Test
	public void testFindByTag() throws Exception {
		ITag parent = new Tag();
		parent.setTitle("Root");
		repository.save(parent);
		ITag child = new Tag();
		child.setTitle("Sub 1");
		repository.save(child);

		// now create an entity
		ODocument document1 = new ODocument("Node").field("title", "title 1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L)
				.save();

		INode node1 = new Node();
		node1.setId(document1.getIdentity().toString());

		// now create an entity
		ODocument document2 = new ODocument("Node").field("title", "title 2")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L)
				.save();

		INode node2 = new Node();
		node2.setId(document2.getIdentity().toString());

		// connect nodes and tags
		repository.connectTag(parent, child);
		repository.connectTag(parent, node1);
		repository.connectTag(child, node2);

		// get all nodes of root
		List<SegradaTaggable> list = repository.findByTag(parent.getId(), true, null);
		assertEquals(4, list.size());
		list = repository.findByTag(parent.getId(), true, new String[0]);
		assertEquals(4, list.size());

		// no traversal
		list = repository.findByTag(parent.getId(), false, null);
		assertEquals(2, list.size());
		list = repository.findByTag(parent.getId(), false, new String[0]);
		assertEquals(2, list.size());

		//... of child
		list = repository.findByTag(child.getId(), true, null);
		assertEquals(2, list.size());
		list = repository.findByTag(child.getId(), true, new String[0]);
		assertEquals(2, list.size());

		// no traversal
		list = repository.findByTag(child.getId(), false, null);
		assertEquals(1, list.size());
		list = repository.findByTag(child.getId(), false, new String[0]);
		assertEquals(1, list.size());

		// filter parent
		list = repository.findByTag(parent.getId(), true, new String[]{"Node", "Tag", "Unknown"});
		assertEquals(4, list.size());
		list = repository.findByTag(parent.getId(), true, new String[]{"Node", "Tag"});
		assertEquals(4, list.size());
		list = repository.findByTag(parent.getId(), true, new String[]{"Node"});
		assertEquals(2, list.size());
		list = repository.findByTag(parent.getId(), true, new String[]{"Tag"});
		assertEquals(2, list.size());
		list = repository.findByTag(parent.getId(), true, new String[]{"Unknown"});
		assertEquals(0, list.size());

		// filter parent - no traversal
		list = repository.findByTag(parent.getId(), false, new String[]{"Node", "Tag", "Unknown"});
		assertEquals(2, list.size());
		list = repository.findByTag(parent.getId(), false, new String[]{"Node", "Tag"});
		assertEquals(2, list.size());
		list = repository.findByTag(parent.getId(), false, new String[]{"Node"});
		assertEquals(1, list.size());
		list = repository.findByTag(parent.getId(), false, new String[]{"Tag"});
		assertEquals(1, list.size());
		list = repository.findByTag(parent.getId(), false, new String[]{"Unknown"});
		assertEquals(0, list.size());

		// filter child
		list = repository.findByTag(child.getId(), true, new String[]{"Node"});
		assertEquals(1, list.size());
		list = repository.findByTag(child.getId(), true, new String[]{"Tag"});
		assertEquals(1, list.size());
		list = repository.findByTag(child.getId(), true, new String[]{"Unknown"});
		assertEquals(0, list.size());

		// filter child - no traversal
		list = repository.findByTag(child.getId(), false, new String[]{"Node"});
		assertEquals(1, list.size());
		list = repository.findByTag(child.getId(), false, new String[]{"Tag"});
		assertEquals(0, list.size());
		list = repository.findByTag(child.getId(), false, new String[]{"Unknown"});
		assertEquals(0, list.size());
	}

	@Test
	public void testFindTagIdsConnectedToModel() throws Exception {
		ITag parent = new Tag();
		parent.setTitle("Root");
		repository.save(parent);
		ITag child = new Tag();
		child.setTitle("Sub 1");
		repository.save(child);
		ITag child2 = new Tag();
		child2.setTitle("Sub 2");
		repository.save(child2);

		// now create an entity
		ODocument document1 = new ODocument("Node").field("title", "title 1")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L)
				.save();

		INode node1 = new Node();
		node1.setId(document1.getIdentity().toString());

		// now create an entity
		ODocument document2 = new ODocument("Node").field("title", "title 2")
				.field("alternativeTitles", "alternativeTitles")
				.field("description", "Description")
				.field("descriptionMarkup", "default")
				.field("color", 0x123456)
				.field("created", 1L)
				.field("modified", 2L)
				.save();

		INode node2 = new Node();
		node2.setId(document2.getIdentity().toString());

		// connect nodes and tags
		repository.connectTag(parent, child);
		repository.connectTag(child, node1);
		repository.connectTag(child2, node1);
		repository.connectTag(child, node2);

		// get tags connected to node1
		String[] ids = repository.findTagIdsConnectedToModel(node1, true);
		assertTrue(ids.length == 2);
		ids = repository.findTagIdsConnectedToModel(node1, false);
		assertTrue(ids.length == 3);

		// get tags connected to node2
		ids = repository.findTagIdsConnectedToModel(node2, true);
		assertTrue(ids.length == 1);
		ids = repository.findTagIdsConnectedToModel(node2, false);
		assertTrue(ids.length == 2);
	}

	@Test
	public void testConnectTags() throws Exception {
		ODatabaseDocumentTx db = factory.getDb();

		ITag parent = new Tag();
		parent.setTitle("Root");
		repository.save(parent);
		ITag child = new Tag();
		child.setTitle("Sub 1");
		repository.save(child);

		repository.connectTag(parent, child);

		// check connection
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsTagOf where out = " + parent.getId() + " AND in = " + child.getId());
		List<ODocument> result = db.command(query).execute();

		// there should be one connection
		assertTrue(result.size() == 1);

		// add again
		repository.connectTag(parent, child);

		// there should still be only one connection
		query = new OSQLSynchQuery<>("select @rid from IsTagOf where out = " + parent.getId() + " AND in = " + child.getId());
		result = db.command(query).execute();

		assertTrue(result.size() == 1);

		// try to connect parent as child of child
		try {
			repository.connectTag(child, parent); // circular reference => not allowed
			fail("Circular tags not detected!");
		} catch (RuntimeException e) {
			//OK
		}
	}

	@Test
	public void testIsChildAndParentOf() throws Exception {
		ITag root = new Tag();
		root.setTitle("Root");
		repository.save(root);
		ITag subTag1 = new Tag();
		subTag1.setTitle("Sub 1");
		repository.save(subTag1);
		ITag subTag2 = new Tag();
		subTag2.setTitle("Sub 2");
		repository.save(subTag2);
		ITag sub2Tag1 = new Tag();
		sub2Tag1.setTitle("Sub 2 => Sub 1");
		repository.save(sub2Tag1);
		ITag sub2Tag2 = new Tag();
		sub2Tag2.setTitle("Sub 2 => Sub 2");
		repository.save(sub2Tag2);
		ITag notConnected = new Tag();
		notConnected.setTitle("Not connected");
		repository.save(notConnected);

		// add connections
		repository.connectTag(root, subTag1);
		repository.connectTag(root, subTag2);
		repository.connectTag(subTag2, sub2Tag1);
		repository.connectTag(subTag2, sub2Tag2);

		// check whole tree
		assertTrue(repository.isChildOf(subTag1, root));
		assertTrue(repository.isChildOf(subTag2, root));
		assertTrue(repository.isChildOf(sub2Tag1, subTag2));
		assertTrue(repository.isChildOf(sub2Tag2, subTag2));
		assertTrue(repository.isChildOf(sub2Tag1, root));
		assertTrue(repository.isChildOf(sub2Tag2, root));

		// check false stuff
		assertFalse(repository.isChildOf(root, root));
		assertFalse(repository.isChildOf(root, subTag1));
		assertFalse(repository.isChildOf(root, subTag2));
		assertFalse(repository.isChildOf(root, sub2Tag1));
		assertFalse(repository.isChildOf(notConnected, root));

		// check parent of
		assertTrue(repository.isParentOf(root, subTag1));
		assertTrue(repository.isParentOf(root, subTag2));
		assertTrue(repository.isParentOf(subTag2, sub2Tag1));
		assertTrue(repository.isParentOf(subTag2, sub2Tag2));
		assertTrue(repository.isParentOf(root, sub2Tag1));
		assertTrue(repository.isParentOf(root, sub2Tag2));

		// check false stuff
		assertFalse(repository.isParentOf(root, root));
		assertFalse(repository.isParentOf(subTag1, root));
		assertFalse(repository.isParentOf(subTag2, root));
		assertFalse(repository.isParentOf(sub2Tag1, root));
		assertFalse(repository.isParentOf(root, notConnected));
	}

	@Test
	public void testRemoveTag() throws Exception {
		ITag root = new Tag();
		root.setTitle("Root");
		repository.save(root);
		ITag subTag1 = new Tag();
		subTag1.setTitle("Sub 1");
		repository.save(subTag1);

		// must not throw an exception when there is no connection
		repository.removeTag(root.getId(), subTag1);

		// add connection
		repository.connectTag(root, subTag1);

		// check if connection was set
		assertTrue(repository.isChildOf(subTag1, root));

		// remove tag
		repository.removeTag(root.getId(), subTag1);

		// check if connection was removed
		assertFalse(repository.isChildOf(subTag1, root));
	}

	@Test
	public void testPaginate() throws Exception {
		fail("Test not implemented yet.");
	}
}