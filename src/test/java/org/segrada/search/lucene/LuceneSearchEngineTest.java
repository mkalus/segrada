package org.segrada.search.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.segrada.search.SearchHit;
import org.segrada.service.util.PaginationInfo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LuceneSearchEngineTest {
	/**
	 * Reference to search engine
	 */
	private LuceneSearchEngine searchEngine;

	@Before
	public void setUp() throws Exception {
		searchEngine = new LuceneSearchEngine(new RAMDirectory(), new StandardAnalyzer(Version.LUCENE_47));
	}

	@Test
	public void testIndexSearchRemove() throws Exception {
		// index
		boolean check = searchEngine.index("1", "DummyClass", "Hello World", "Another Title\nAnd my subtitle",
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
						"labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
						"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum " +
						"dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
						"eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero " +
						"eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
						"sanctus est Lorem ipsum dolor sit amet.", new String[]{}, null, null,1.0f);

		assertTrue("Could not save document to index", check);

		// search
		PaginationInfo<SearchHit> searchResult = searchEngine.search("consetetur hello", null);

		assertEquals(1, searchResult.total);
		assertEquals(1, searchResult.getMinPage());
		assertEquals(1, searchResult.entities.size());

		// get hit and analyze it
		SearchHit hit = searchResult.entities.get(0);

		assertEquals("1", hit.getId());
		assertEquals("DummyClass", hit.getClassName());
		assertEquals("Hello World", hit.getTitle());
		assertEquals("Another Title\nAnd my subtitle", hit.getSubTitles());
		assertTrue(hit.getRelevance() > 0);
		assertEquals(2, hit.getHighlightText().length);

		// now remove entry
		searchEngine.remove("1");

		// search - should not return anything
		searchResult = searchEngine.search("consetetur hello", null);

		assertEquals(0, searchResult.total);
	}

	@Test
	public void testSearchFilters() throws Exception {
		// index
		searchEngine.index("1", "DummyClass", "Title", "Subtitle Another Avocado",
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
						"labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
						"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum " +
						"dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
						"eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero " +
						"eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
						"sanctus est Lorem ipsum dolor sit amet.", new String[]{"tag1", "tag2", "tag4"}, null, null, 1.0f);
		searchEngine.index("2", "AnotherDummyClass", "Title 2 Avocado", "This is a completely different Subtitle",
				"Little text.", new String[]{"tag1"}, null, null, 1.0f);

		PaginationInfo<SearchHit> searchResult;
		Map<String, String> filter = new HashMap<>();

		// search - no filter
		searchResult = searchEngine.search("title", null);
		assertEquals(2, searchResult.total);
		searchResult = searchEngine.search("Avocado", null);
		assertEquals(2, searchResult.total);
		searchResult = searchEngine.search("text", null);
		assertEquals(1, searchResult.total);
		searchResult = searchEngine.search("DOESNOTCOMPUTE", null);
		assertEquals(0, searchResult.total);

		// test invalid search filter
		filter.clear();
		filter.put("fields", "noodles");
		searchResult = searchEngine.search("title", filter);
		assertEquals(0, searchResult.total);

		// test title search
		filter.clear();
		filter.put("fields", "title");
		searchResult = searchEngine.search("title", filter);
		assertEquals(2, searchResult.total);
		searchResult = searchEngine.search("Avocado", filter);
		assertEquals(1, searchResult.total);
		searchResult = searchEngine.search("text", filter);
		assertEquals(0, searchResult.total);

		// test subtitles search
		filter.clear();
		filter.put("fields", "subtitles");
		searchResult = searchEngine.search("Subtitle", filter);
		assertEquals(2, searchResult.total);
		searchResult = searchEngine.search("Avocado", filter);
		assertEquals(1, searchResult.total);
		searchResult = searchEngine.search("text", filter);
		assertEquals(0, searchResult.total);

		// test text search
		filter.clear();
		filter.put("fields", "content");
		searchResult = searchEngine.search("text", filter);
		assertEquals(1, searchResult.total);
		searchResult = searchEngine.search("Avocado", filter);
		assertEquals(0, searchResult.total); // titles contained in content
		searchResult = searchEngine.search("Title", filter);
		assertEquals(0, searchResult.total); // titles contained in content

		// title and subtitle fields
		filter.clear();
		filter.put("fields", "allTitles");
		searchResult = searchEngine.search("text", filter);
		assertEquals(0, searchResult.total);
		searchResult = searchEngine.search("Avocado", filter);
		assertEquals(2, searchResult.total); // titles contained in content
		searchResult = searchEngine.search("Title", filter);
		assertEquals(2, searchResult.total); // titles contained in content
		searchResult = searchEngine.search("Subtitle", filter);
		assertEquals(2, searchResult.total); // titles contained in content

		// test classes
		filter.clear();
		filter.put("class", "DummyClass");
		searchResult = searchEngine.search("title", filter);
		assertEquals(1, searchResult.total);
		filter.put("class", "DummyClassNOTEXIST");
		searchResult = searchEngine.search("title", filter);
		assertEquals(0, searchResult.total);

		// limit filter
		filter.clear();
		filter.put("limit", "1");
		searchResult = searchEngine.search(null, filter);
		assertEquals(1, searchResult.entities.size());

		// operator filter
		filter.clear();
		filter.put("operator", "and");
		searchResult = searchEngine.search("title another", filter);
		assertEquals(1, searchResult.entities.size());
		filter.clear();
		filter.put("operator", "or");
		searchResult = searchEngine.search("title another", filter);
		assertEquals(2, searchResult.entities.size());

		// single tag filter
		filter.clear();
		filter.put("tags", "tag1");
		searchResult = searchEngine.search(null, filter);
		assertEquals(2, searchResult.entities.size());

		// single tag filter
		filter.clear();
		filter.put("tags", "tag2");
		searchResult = searchEngine.search(null, filter);
		assertEquals(1, searchResult.entities.size());

		// single tag filter - non-existent
		filter.clear();
		filter.put("tags", "tag66");
		searchResult = searchEngine.search(null, filter);
		assertEquals(0, searchResult.entities.size());

		// multi-tag-filter
		filter.clear();
		filter.put("tags", "tag1,tag2");
		searchResult = searchEngine.search(null, filter);
		assertEquals(2, searchResult.entities.size());

		// multi-tag-filter with non-existent
		filter.clear();
		filter.put("tags", "tag2,tag66");
		searchResult = searchEngine.search(null, filter);
		assertEquals(1, searchResult.entities.size());

		// multi-tag-filter with different order
		filter.clear();
		filter.put("tags", "tag4,tag2");
		searchResult = searchEngine.search(null, filter);
		assertEquals(1, searchResult.entities.size());

		// multiple filter test
		filter.clear();
		filter.put("tags", "tag1");
		filter.put("class", "DummyClass");
		searchResult = searchEngine.search(null, filter);
		assertEquals(1, searchResult.total);

		searchResult = searchEngine.search("labore", filter);
		assertEquals(1, searchResult.total);
	}

	@Test
	public void testGetById() throws Exception {
		// index
		searchEngine.index("2", "DummyClass", "Hello World", "Another Title\nAnd my subtitle",
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
						"labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
						"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor " +
						"sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
						"tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et " +
						"accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus " +
						"est Lorem ipsum dolor sit amet.", new String[]{"12-5", "12-8", "12-99"}, null, null, 1.0f);

		SearchHit hit = searchEngine.getById("2");

		assertNotNull(hit);
		assertEquals("2", hit.getId());
		assertEquals("DummyClass", hit.getClassName());
		assertEquals("Hello World", hit.getTitle());
		assertEquals("Another Title\n" +
				"And my subtitle", hit.getSubTitles());
		assertEquals("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
						"tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et " +
						"accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus " +
						"est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed " +
						"diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
						"At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea " +
						"takimata sanctus est Lorem ipsum dolor sit amet.",
				hit.getHighlightText()[0]);
	}

	@Test
	public void testEmptyTags() throws Exception {
		searchEngine.index("2b", "DummyClass", "Hello World", "Another Title\nAnd my subtitle",
				"Text.", new String[]{}, null, null, 1.0f);

		SearchHit hit = searchEngine.getById("2b");
		assertNotNull(hit.getTagIds());
		assertArrayEquals(new String[]{}, hit.getTagIds());

		// null value
		searchEngine.index("2c", "DummyClass", "Hello World", "Another Title\nAnd my subtitle",
				"Text.", null, null, null, 1.0f);

		hit = searchEngine.getById("2c");
		assertNotNull(hit.getTagIds());
		assertArrayEquals(new String[]{}, hit.getTagIds());
	}

	@Test
	public void testClearAll() throws Exception {
		// index
		searchEngine.index("3", "DummyClass", "Hello World", "Another Title\nAnd my subtitle",
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
						" labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
						"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor " +
						"sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
						"tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et " +
						"accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus " +
						"est Lorem ipsum dolor sit amet.", new String[]{}, null, null, 1.0f);

		searchEngine.clearAllIndexes();

		// search
		PaginationInfo<SearchHit> searchResult = searchEngine.search("consetetur hello", null);
		assertEquals(0, searchResult.total);

		SearchHit hit = searchEngine.getById("3");
		assertNull(hit);
	}

	@Test
	public void testDoubleAdd() throws Exception {
		// adding two entities with the same id should produce only one entry in db

		// clear all
		searchEngine.clearAllIndexes();

		// index twice
		searchEngine.index("4", "DummyClass", "Hello World 1", "xyzzy", "xyzzy", new String[]{}, null, null, 1.0f);
		searchEngine.index("4", "DummyClass", "Hello World 2", "xyzzy", "xyzzy", new String[]{}, null, null, 1.0f);

		// search
		PaginationInfo<SearchHit> searchResult = searchEngine.search("xyzzy", null);

		assertEquals(1, searchResult.total);
		assertEquals(1, searchResult.getMinPage());
		assertEquals(1, searchResult.getMaxPage());
		assertEquals(1, searchResult.entities.size());

		// get hit and analyze it
		SearchHit hit = searchResult.entities.get(0);
		assertEquals("Hello World 2", hit.getTitle()); // should be second title which has been saved
	}

	@Test
	public void testSearchInDocument() throws Exception {
		// index
		searchEngine.index("4", "DummyClass", "Hello World", "Another Title\nAnd my subtitle",
				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
						"x labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
						"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor " +
						"sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
						"tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et " +
						"accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus " +
						"est Lorem ipsum dolor sit amet.", new String[]{}, null, null, 1.0f);

		// assert null and empty values
		assertEquals(0, searchEngine.searchInDocument("", "").length);
		assertEquals(0, searchEngine.searchInDocument(null, null).length);
		assertEquals(0, searchEngine.searchInDocument("", null).length);
		assertEquals(0, searchEngine.searchInDocument(null, "").length);

		// now check normal operation
		assertEquals(0, searchEngine.searchInDocument("notExistant", "4").length);

		String[] highlights = searchEngine.searchInDocument("labore", "4");

		assertEquals(2, highlights.length);
		assertEquals("elitr, sed diam nonumy eirmod tempor invidunt ut x <b>labore</b> et dolore magna aliquyam erat, sed diam voluptua", highlights[0]);
		assertEquals("elitr, sed diam nonumy eirmod tempor invidunt ut <b>labore</b> et dolore magna aliquyam erat, sed diam voluptua", highlights[1]);
	}
}