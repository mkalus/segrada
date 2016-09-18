package org.segrada.service.base;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.model.Source;
import org.segrada.model.prototype.ISource;
import org.segrada.search.SearchEngine;
import org.segrada.search.SearchHit;
import org.segrada.search.lucene.LuceneSearchEngine;
import org.segrada.search.lucene.LuceneSegradaAnalyzer;
import org.segrada.service.repository.SourceRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.Identity;
import org.segrada.test.OrientDBTestInstance;
import org.segrada.test.OrientDbTestApplicationSettings;

import javax.annotation.Nullable;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractFullTextServiceTest {
	/**
	 * reference to test instance of orientdb in memory
	 */
	private OrientDBTestInstance orientDBTestInstance = new OrientDBTestInstance();

	/**
	 * reference to factory
	 */
	private OrientDbRepositoryFactory factory;

	/**
	 * service to test
	 */
	private MockService service;

	/**
	 * Reference to search engine
	 */
	private LuceneSearchEngine searchEngine;

	/**
	 * flag
	 */
	private boolean methodCalled;

	@Before
	public void setUp() throws Exception {
		searchEngine = new LuceneSearchEngine(new RAMDirectory(), new LuceneSegradaAnalyzer());

		// set up schema if needed
		orientDBTestInstance.setUpSchemaIfNeeded();

		// open database
		ODatabaseDocumentTx db = orientDBTestInstance.getDatabase();

		factory = new OrientDbRepositoryFactory(db, new OrientDbTestApplicationSettings(), new Identity());

		// create service to test
		service = new MockService(factory, searchEngine);
	}

	@After
	public void tearDown() throws Exception {
		// clear search indexes
		searchEngine.clearAllIndexes();

		// truncate db
		factory.getDb().command(new OCommandSQL("delete vertex V")).execute();
		factory.getDb().command(new OCommandSQL("truncate class File")).execute();

		// close db
		try {
			factory.getDb().close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testSave() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setLongTitle("longTitle");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");

		assertTrue(service.save(source));

		// check existence
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from " + source.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertFalse(result.isEmpty());

		// in index?
		SearchHit hit = searchEngine.getById(source.getUid());
		assertNotNull(hit);
	}

	@Test
	public void testDelete() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setLongTitle("longTitle");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");

		service.save(source);

		// delete it
		service.delete(source);

		// check existence
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from " + source.getId());
		List<ODocument> result = factory.getDb().command(query).execute();

		assertTrue(result.isEmpty());

		// in index?
		SearchHit hit = searchEngine.getById(source.getUid());
		assertNull(hit);
	}

	@Test
	public void testReindexAll() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setLongTitle("longTitle");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");

		service.save(source);

		searchEngine.clearAllIndexes();

		SearchHit hit = searchEngine.getById(source.getUid());
		assertNull(hit);

		service.reindexAll();

		// in index?
		hit = searchEngine.getById(source.getUid());
		assertNotNull(hit);
	}

	@Test
	public void testIndexEntity() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setLongTitle("longTitle");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");

		service.save(source);

		searchEngine.clearAllIndexes();

		SearchHit hit = searchEngine.getById(source.getUid());
		assertNull(hit);

		service.indexEntity(source);

		// in index?
		hit = searchEngine.getById(source.getUid());
		assertNotNull(hit);
	}

	@Test
	public void testPrepareIndexEntity() throws Exception {
		methodCalled = false;

		ISource source = new Source();
		source.setShortTitle("shortTitle");
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

		AbstractFullTextService.SearchIndexEntity entity = service.prepareIndexEntity(source);
		assertTrue(methodCalled);
		assertNotNull(entity);
	}

	@Test
	public void testSaveToSearchIndex() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setLongTitle("longTitle");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");

		service.save(source);

		searchEngine.clearAllIndexes();
		AbstractFullTextService.SearchIndexEntity entity = service.prepareIndexEntity(source);

		service.saveToSearchIndex(entity);

		SearchHit hit = searchEngine.getById(source.getUid());
		assertNotNull(hit);
	}

	@Test
	public void testRemoveFromSearchIndex() throws Exception {
		ISource source = new Source();
		source.setShortTitle("shortTitle");
		source.setShortRef("shortRef");
		source.setLongTitle("longTitle");
		source.setDescription("Description");
		source.setDescriptionMarkup("default");

		service.save(source);

		SearchHit hit = searchEngine.getById(source.getUid());
		assertNotNull(hit);

		service.removeFromSearchIndex(source);

		hit = searchEngine.getById(source.getUid());
		assertNull(hit);
	}

	/**
	 * mock service class based on source repository - should be a mock repository, but I am too lazy now
	 */
	private class MockService extends AbstractFullTextService<ISource, SourceRepository> implements SearchTermService<ISource> {
		/**
		 * Constructor
		 */
		@Inject
		public MockService(RepositoryFactory repositoryFactory, SearchEngine searchEngine) {
			super(repositoryFactory, SourceRepository.class, searchEngine);
		}

		@Override
		public ISource createNewInstance() {
			return new Source();
		}

		@Override
		public Class<ISource> getModelClass() {
			return ISource.class;
		}

		/**
		 * Find entities by search term
		 *
		 * @param term              search term (or empty)
		 * @param maximum           maximum hits to return
		 * @param returnWithoutTerm true if you want to return top hits if no search term is supplied (otherwise empty)
		 * @return list of entities
		 */
		public List<ISource> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
			return repository.findBySearchTerm(term, maximum, returnWithoutTerm);
		}

		@Override
		public List<ISource> search(String term) {
			return findBySearchTerm(term, 10, true);
		}

		@Nullable
		@Override
		protected SearchIndexEntity prepareIndexEntity(ISource entity) {
			SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getUid());
			idxEntity.title = entity.getShortTitle();
			String subTitles = entity.getCitation();
			if (subTitles == null || subTitles.isEmpty()) subTitles = entity.getTitle();
			if (subTitles == null || subTitles.isEmpty()) subTitles = entity.getAuthor();
			idxEntity.subTitles = subTitles;
			idxEntity.content = entity.getDescription();
			idxEntity.contentMarkup = entity.getDescriptionMarkup();
			idxEntity.weight = 1f; // not so important

			// get tag ids and add them to entity
			TagRepository tagRepository = repositoryFactory.produceRepository(TagRepository.class);
			if (tagRepository != null)
				idxEntity.tagIds = tagRepository.findTagIdsConnectedToModel(entity, false);

			// set flag
			methodCalled = true;

			return idxEntity;
		}

		/**
		 * Find entities by title
		 *
		 * @param ref reference title
		 * @return entity or null
		 */
		public ISource findByRef(String ref) {
			return repository.findByRef(ref);
		}

		/**
		 * Find entities by title
		 *
		 * @param title short title
		 * @return entity or null
		 */
		public List<ISource> findByTitle(String title) {
			return repository.findByTitle(title);
		}
	}
}