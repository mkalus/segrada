package org.segrada.service.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class PaginationInfoTest {

	@Test
	public void testGetMinPage() throws Exception {
		PaginationInfo paginationInfo = new PaginationInfo(1, 1, 20, 20, null);
		assertEquals(1, paginationInfo.getMinPage());

		paginationInfo = new PaginationInfo(1, 10, 200, 20, null);
		assertEquals(1, paginationInfo.getMinPage());

		paginationInfo = new PaginationInfo(5, 5, 100, 20, null);
		assertEquals(2, paginationInfo.getMinPage());

		paginationInfo = new PaginationInfo(5, 10, 200, 20, null);
		assertEquals(2, paginationInfo.getMinPage());
	}

	@Test
	public void testGetMaxPage() throws Exception {
		PaginationInfo paginationInfo = new PaginationInfo(1, 1, 20, 20, null);
		assertEquals(1, paginationInfo.getMaxPage());

		paginationInfo = new PaginationInfo(1, 10, 200, 20, null);
		assertEquals(4, paginationInfo.getMaxPage());

		paginationInfo = new PaginationInfo(5, 5, 100, 20, null);
		assertEquals(5, paginationInfo.getMaxPage());

		paginationInfo = new PaginationInfo(5, 10, 200, 20, null);
		assertEquals(8, paginationInfo.getMaxPage());
	}

	@Test
	public void testShowFirstPage() throws Exception {
		PaginationInfo paginationInfo = new PaginationInfo(1, 1, 20, 20, null);
		assertEquals(false, paginationInfo.showFirstPage());

		paginationInfo = new PaginationInfo(1, 10, 200, 20, null);
		assertEquals(false, paginationInfo.showFirstPage());

		paginationInfo = new PaginationInfo(5, 5, 100, 20, null);
		assertEquals(true, paginationInfo.showFirstPage());

		paginationInfo = new PaginationInfo(5, 10, 200, 20, null);
		assertEquals(true, paginationInfo.showFirstPage());
	}

	@Test
	public void testShowLastPage() throws Exception {
		PaginationInfo paginationInfo = new PaginationInfo(1, 1, 20, 20, null);
		assertEquals(false, paginationInfo.showLastPage());

		paginationInfo = new PaginationInfo(1, 10, 200, 20, null);
		assertEquals(true, paginationInfo.showLastPage());

		paginationInfo = new PaginationInfo(5, 5, 100, 20, null);
		assertEquals(false, paginationInfo.showLastPage());

		paginationInfo = new PaginationInfo(5, 10, 200, 20, null);
		assertEquals(true, paginationInfo.showLastPage());
	}
}