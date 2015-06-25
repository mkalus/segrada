package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.segrada.model.prototype.*;
import org.segrada.service.repository.CommentRepository;
import org.segrada.service.repository.FileRepository;
import org.segrada.service.repository.SourceReferenceRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.OrientDbCommentRepository;
import org.segrada.service.repository.orientdb.OrientDbFileRepository;
import org.segrada.service.repository.orientdb.OrientDbSourceReferenceRepository;
import org.segrada.service.repository.orientdb.OrientDbTagRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(AbstractAnnotatedOrientDbRepository.class);

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
		if (entity.getId() != null) {
			// set tags
			entity.setTags(lazyLoadTags(entity));
			// set source references
			entity.setSourceReferences(lazyLoadSourceReferences(entity));
			// set source references
			entity.setComments(lazyLoadComments(entity));
			// set source references
			entity.setFiles(lazyLoadFiles(entity));
		}
	}

	/**
	 * lazily load source references for an entity
	 * @param entity connected as reference
	 * @return list of source references (proxy)
	 */
	@SuppressWarnings("unchecked")
	public List<ISourceReference> lazyLoadSourceReferences(final SegradaAnnotatedEntity entity) {
		try {
			return (List<ISourceReference>) java.lang.reflect.Proxy.newProxyInstance(
					List.class.getClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							SourceReferenceRepository sourceReferenceRepository =
									repositoryFactory.produceRepository(OrientDbSourceReferenceRepository.class);

							return sourceReferenceRepository.findByReference(entity.getId());
						}
					}
			);
		} catch (Exception e) {
			logger.error("Could not lazy load ISourceReferences for " + entity.toString(), e);
		}
		return null;
	}

	/**
	 * lazily load source references for an entity
	 * @param entity connected as reference
	 * @return list of source references (proxy)
	 */
	@SuppressWarnings("unchecked")
	public List<IComment> lazyLoadComments(final SegradaAnnotatedEntity entity) {
		try {
			return (List<IComment>) java.lang.reflect.Proxy.newProxyInstance(
					List.class.getClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							CommentRepository commentRepository =
									repositoryFactory.produceRepository(OrientDbCommentRepository.class);

							return commentRepository.findByReference(entity.getId());
						}
					}
			);
		} catch (Exception e) {
			logger.error("Could not lazy load IComments for " + entity.toString(), e);
		}
		return null;
	}

	/**
	 * lazily load source references for an entity
	 * @param entity connected as reference
	 * @return list of source references (proxy)
	 */
	@SuppressWarnings("unchecked")
	public List<IFile> lazyLoadFiles(final SegradaAnnotatedEntity entity) {
		try {
			return (List<IFile>) java.lang.reflect.Proxy.newProxyInstance(
					List.class.getClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							FileRepository fileRepository =
									repositoryFactory.produceRepository(OrientDbFileRepository.class);

							return fileRepository.findByReference(entity.getId());
						}
					}
			);
		} catch (Exception e) {
			logger.error("Could not lazy load IFiles for " + entity.toString(), e);
		}
		return null;
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
		String[] tagIds = tagRepository.findTagIdsConnectedToModel(entity, true);
		for (String id : tagIds) {
			if (!addedIds.contains(id)) // not in set connected - delete
				tagRepository.removeTag(id, entity);
		}
	}

	@Override
	public boolean delete(T entity) {
		if (super.delete(entity)) {
			// delete connected locations and periods
			//db.command(new OCommandSQL("delete from Location where parent = " + entity.getId())).execute();
			//db.command(new OCommandSQL("delete from Period where parent = " + entity.getId())).execute();

			//TODO: delete source reference pointing to me, too
			//TODO: delete tag links? comment links? file links?

			return true;
		}
		return false;
	}
}
