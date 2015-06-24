package org.segrada.service.util;

import org.segrada.model.prototype.SegradaEntity;

import java.util.List;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
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
public class PaginationInfo<BEAN extends SegradaEntity> {
	/**
	 * current page to show
	 */
	public final int page;

	/**
	 * total number of pages
	 */
	public final int pages;

	/**
	 * total number of entities
	 */
	public final int total;

	/**
	 * entries per page to show
	 */
	public final int entriesPerPage;

	/**
	 * hits
	 */
	public final List<BEAN> entities;

	/**
	 * constructor
	 */
	public PaginationInfo(int page, int pages, int total, int entriesPerPage, List<BEAN> entities) {
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
}
