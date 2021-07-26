package org.segrada.service.util;

import org.segrada.model.prototype.SegradaEntity;

import java.util.List;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Class holding pagination information of a certain set
 */
public class PaginationInfo<T extends SegradaEntity> {
	/**
	 * current page to show
	 */
	private final int page;

	/**
	 * total number of pages
	 */
	private final int pages;

	/**
	 * total number of entities
	 */
	private final int total;

	/**
	 * entries per page to show
	 */
	private final int entriesPerPage;

	/**
	 * hits
	 */
	private final List<T> entities;

	/**
	 * constructor
	 */
	public PaginationInfo(int page, int pages, int total, int entriesPerPage, List<T> entities) {
		this.page = page;
		this.pages = pages;
		this.total = total;
		this.entriesPerPage = entriesPerPage;
		this.entities = entities;
	}

	/**
	 * get minimum page
	 * @return minimum page to show
	 */
	public int getMinPage() {
		int minPage = page - 3;
		if (minPage < 1) minPage = 1;

		return minPage;
	}

	/**
	 * get maximum page
	 * @return maximum page to show
	 */
	public int getMaxPage() {
		int maxPage = page + 3;
		if (maxPage > pages) maxPage = pages;

		return maxPage;
	}

	/**
	 * @return true if there should be a button to show the first page
	 */
	public boolean showFirstPage() {
		return getMinPage()!=1;
	}

	/**
	 * @return true if there should be a button to show the last page
	 */
	public boolean showLastPage() {
		return getMaxPage()!=pages;
	}

	/**
	 * @return translation key handling singular and plural forms
	 */
	public String key() {
		switch (total) {
			case 0: return "paginationNone";
			case 1: return "paginationOne";
			default: break;
		}

		return "pagination";
	}

	public int getPage() {
		return page;
	}

	public int getPages() {
		return pages;
	}

	public int getTotal() {
		return total;
	}

	public int getEntriesPerPage() {
		return entriesPerPage;
	}

	public List<T> getEntities() {
		return entities;
	}
}
