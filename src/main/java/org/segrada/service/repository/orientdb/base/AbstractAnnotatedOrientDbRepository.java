package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.OrientDbTagRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * Abstract OrientDb Repository for annotated entities
 */
abstract public class AbstractAnnotatedOrientDbRepository<T extends SegradaAnnotatedEntity> extends AbstractColoredOrientDbRepository<T> {
	/**
	 * Constructor
	 */
	public AbstractAnnotatedOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	protected void populateODocumentWithAnnotated(ODocument document, SegradaAnnotatedEntity entity) {
		//tags are relations and set/updated in processAfterSaving
	}

	protected void populateEntityWithAnnotated(ODocument document, SegradaAnnotatedEntity entity) {
		TagRepository tagRepository = repositoryFactory.produceRepository(OrientDbTagRepository.class);

		// get connected tags
		if (entity.getId() != null & tagRepository != null) {
			String[] tagIds = tagRepository.findTagIdsConnectedToModel(entity.getId(), entity.getClass().getSimpleName(), true);
			entity.setTags(tagRepository.findTagTitlesByIds(tagIds));
		}
	}

	@Override
	protected T processAfterSaving(ODocument updated, T entity) {
		entity = super.processAfterSaving(updated, entity);

		// connect tags
		updateEntityTags(entity);

		return entity;
	}

	/**
	 * update tag connections of a given entity
	 * will delete old tag connections, create new tags and update them
	 * @param entity to update tags from
	 */
	protected void updateEntityTags(SegradaAnnotatedEntity entity) {
		TagRepository tagRepository = repositoryFactory.produceRepository(OrientDbTagRepository.class);

		// return, of not tags set
		if (tagRepository == null || entity.getTags() == null || entity.getTags().length == 0) return;

		// create new tags, if needed
		tagRepository.createNewTagsByTitles(entity.getTags());

		// find all tags by title
		List<ITag> tags = tagRepository.findTagsByTitles(entity.getTags());

		// keeps added ids
		Set<String> addedIds = new HashSet<>();

		// add all tags to entity
		for (ITag tag : tags) {
			tagRepository.connectTag(tag, entity);
			addedIds.add(tag.getId());
		}

		// now find all tag ids and see if there are some that have been deleted
		String[] tagIds = tagRepository.findTagIdsConnectedToModel(entity.getId(), entity.getClass().getSimpleName(), true);
		for (String id : tagIds) {
			if (!addedIds.contains(id)) // not in set connected - delete
				tagRepository.removeTag(id, entity);
		}
	}

	//TODO: more stuff
}
