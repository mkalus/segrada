package org.segrada.service.base;

import org.segrada.model.prototype.SegradaEntity;
import org.segrada.rendering.markup.MarkupFilter;
import org.segrada.rendering.markup.MarkupFilterFactory;
import org.segrada.search.SearchEngine;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

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
 * Abstract service supporting full text search
 */
abstract public class AbstractFullTextService<T extends SegradaEntity, E extends CRUDRepository<T>> extends AbstractRepositoryService<T, E> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractFullTextService.class);

	/**
	 * reference to search engine
	 */
	private final SearchEngine searchEngine;

	/**
	 * Constructor
	 */
	public AbstractFullTextService(RepositoryFactory repositoryFactory, Class clazz, SearchEngine searchEngine) {
		super(repositoryFactory, clazz);
		this.searchEngine = searchEngine;
	}

	@Override
	public boolean save(T entity) {
		if (super.save(entity)) {
			indexEntity(entity);

			return true;
		}
		return false;
	}

	@Override
	public boolean delete(T entity) {
		removeFromSearchIndex(entity);
		return super.delete(entity);
	}

	/**
	 * reindex all entities
	 */
	public void reindexAll() {
		findAll().forEach(this::indexEntity);
	}

	/**
	 * worker to index entity
	 * @param entity to index
	 */
	protected void indexEntity(T entity) {
		SearchIndexEntity searchIndexEntity = prepareIndexEntity(entity);
		if (searchIndexEntity != null)
			saveToSearchIndex(searchIndexEntity);
	}

	/**
	 * prepare entity for indexing
	 * @param entity to be indexed
	 * @return SearchIndexEntity or null
	 */
	abstract protected @Nullable SearchIndexEntity prepareIndexEntity(T entity);

	/**
	 * commit converted entity (prepareEntityForSearch) to search index - called by save method
	 * @param entity prepared entity for search engine
	 */
	protected void saveToSearchIndex(SearchIndexEntity entity) {
		// sanity check
		if (entity == null || entity.id == null) return;

		// get correct markup filter
		MarkupFilter markupFilter;
		try {
			markupFilter = MarkupFilterFactory.produce(entity.contentMarkup);
		} catch (Exception e) {
			logger.warn("Could not load MarkupFilter " + entity.contentMarkup + " while preparing search index indexing", e);
			return;
		}

		// to index
		if (!searchEngine.index(
				entity.id,
				getModelClass().getSimpleName().substring(1), // clip off the I of the interface
				entity.title,
				entity.subTitles,
				markupFilter.toPlain(entity.content), // convert content to plain, searchable text
				entity.tagIds,
				entity.color,
				entity.iconFileIdentifier,
				entity.weight
		))
			logger.error("Could not write entity to search index: " + entity.getClass().toString() + "/" + entity.id);
		else if (logger.isInfoEnabled())
			logger.info("Indexed entity to search index: " + entity.getClass().toString() + "/" + entity.id);
	}

	/**
	 * remove entity from search index
	 * @param entity to remove from index
	 */
	protected void removeFromSearchIndex(@Nullable T entity) {
		if (entity != null) {
			searchEngine.remove(entity.getUid());
			if (logger.isInfoEnabled())
				logger.info("Removed entity from search index: " + entity.toString());
		}
	}

	/**
	 * Helper class for search index saving
	 */
	protected class SearchIndexEntity {
		private final String id;

		public String title;

		public String subTitles;

		public String content;

		public String contentMarkup;

		public String[] tagIds = new String[]{};

		public Integer color;

		public String iconFileIdentifier;

		public float weight;

		public SearchIndexEntity(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}
}
