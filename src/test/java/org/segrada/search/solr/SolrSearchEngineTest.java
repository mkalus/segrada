package org.segrada.search.solr;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.segrada.search.lucene.LuceneSegradaAnalyzer;
import org.segrada.session.ApplicationSettingsProperties;

import static org.junit.Assert.*;

public class SolrSearchEngineTest {
	EmbeddedSolrServer server;
	CoreContainer container;

	SolrSearchEngine solrSearchEngine;

	@Before
	public void setUp() throws Exception {
		container = new CoreContainer("src/test/resources/testdata/solr");
		container.load();

		server = new EmbeddedSolrServer(container, "collection1" );

		solrSearchEngine = new SolrSearchEngine(new ApplicationSettingsProperties(), new LuceneSegradaAnalyzer(), server);
	}

	@After
	public void tearDown() throws Exception {
		server.close();
	}

	@Test
	public void index() throws Exception {
		// test indexing
		assertTrue(solrSearchEngine.index("test1", "TestClass", "title", "subTitles", "content", new String[]{ "tag", "tag2" }, 12345, null, 1));

		//TODO: not working
	}

	@Test
	public void search() throws Exception {
		fail("Not implemented");
	}

	@Test
	public void searchInDocument() throws Exception {
		fail("Not implemented");
	}

	@Test
	public void remove() throws Exception {
		fail("Not implemented");
	}

	@Test
	public void getById() throws Exception {
		fail("Not implemented");
	}

	@Test
	public void clearAllIndexes() throws Exception {
		fail("Not implemented");
	}

}