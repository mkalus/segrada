package org.segrada.search;

import org.junit.Test;

import static org.junit.Assert.*;

public class SearchResultTest {

	@Test
	public void testGetTotalHits() throws Exception {
		final SearchResult searchResult = new SearchResult();

		searchResult.totalHits = 10;
		assertEquals(10, searchResult.getTotalHits());
	}

	@Test
	public void testSetTotalHits() throws Exception {
		final SearchResult searchResult = new SearchResult();

		searchResult.setTotalHits(10);
		assertEquals(10, searchResult.totalHits);
	}

	@Test
	public void testGetCount() throws Exception {
		final SearchResult searchResult = new SearchResult();

		assertEquals(0, searchResult.getCount());

		// add hits
		searchResult.addHit(new SearchHit());
		assertEquals(1, searchResult.getCount());
	}

	@Test
	public void testGetStartIndex() throws Exception {
		final SearchResult searchResult = new SearchResult();

		searchResult.startIndex = 10;
		assertEquals(0, searchResult.getStartIndex());

		searchResult.addHit(new SearchHit());
		assertEquals(10, searchResult.getStartIndex());
	}

	@Test
	public void testSetStartIndex() throws Exception {
		final SearchResult searchResult = new SearchResult();

		assertEquals(0, searchResult.getStartIndex());

		searchResult.setStartIndex(10);
		assertEquals(10, searchResult.startIndex);
	}

	@Test
	public void testGetStopIndex() throws Exception {
		final SearchResult searchResult = new SearchResult();

		assertEquals(0, searchResult.getStopIndex());

		searchResult.addHit(new SearchHit());
		assertEquals(1, searchResult.getStopIndex());

		searchResult.addHit(new SearchHit());
		assertEquals(2, searchResult.getStopIndex());

		searchResult.setStartIndex(10);
		assertEquals(11, searchResult.getStopIndex());
	}

	@Test
	public void testGetHits() throws Exception {
		final SearchResult searchResult = new SearchResult();

		assertTrue(searchResult.getHits().isEmpty());

		SearchHit hit = new SearchHit();
		searchResult.addHit(hit);
		assertEquals(1, searchResult.getHits().size());
		assertSame(hit, searchResult.getHits().get(0));
	}

	@Test
	public void testAddHit() throws Exception {
		final SearchResult searchResult = new SearchResult();

		assertNull(searchResult.hits);

		searchResult.addHit(new SearchHit());
		assertNotNull(searchResult.hits);
		assertEquals(1, searchResult.hits.size());
	}
}