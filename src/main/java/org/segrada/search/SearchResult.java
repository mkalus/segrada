package org.segrada.search;

import java.util.LinkedList;
import java.util.List;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Representation of a search result
 */
public class SearchResult {
	/**
	 * total hits in search result
	 */
	protected int totalHits;

	/**
	 * start with this index (starting at 1)
	 */
	protected int startIndex = 1;

	/**
	 * hits themselves
	 */
	protected List<SearchHit> hits;

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public int getCount() {
		if (this.hits == null) return 0;

		return this.hits.size();
	}

	public int getStartIndex() {
		if (this.hits == null) return 0;

		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getStopIndex() {
		if (this.hits == null) return 0;

		return getStartIndex() + getCount() - 1;
	}

	public List<SearchHit> getHits() {
		if (this.hits == null)
			this.hits = new LinkedList<>();
		return hits;
	}

	public void addHit(SearchHit hit) {
		if (this.hits == null)
			this.hits = new LinkedList<>();
		hits.add(hit);
	}
}
