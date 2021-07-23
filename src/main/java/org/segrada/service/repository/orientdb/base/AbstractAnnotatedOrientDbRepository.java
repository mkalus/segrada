package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.jetbrains.annotations.NotNull;
import org.segrada.model.prototype.*;
import org.segrada.service.repository.CommentRepository;
import org.segrada.service.repository.FileRepository;
import org.segrada.service.repository.SourceReferenceRepository;
import org.segrada.service.repository.orientdb.OrientDbCommentRepository;
import org.segrada.service.repository.orientdb.OrientDbFileRepository;
import org.segrada.service.repository.orientdb.OrientDbSourceReferenceRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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

	protected void populateEntityWithAnnotated(ODocument document, @NotNull SegradaAnnotatedEntity entity) {
		if (entity.getId() != null) {
			// set tags
			String[] tags = getTags(document);
			if (tags != null && tags.length > 0) {
				entity.setTags(tags);
			}
			// set source references
			entity.setSourceReferences(lazyLoadSourceReferences(entity, 1, 1000)); // TODO: should we change this into something more intelligent?
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
	public List<ISourceReference> lazyLoadSourceReferences(final SegradaAnnotatedEntity entity, final int page, final int entriesPerPage) {
		try {
			return (List<ISourceReference>) java.lang.reflect.Proxy.newProxyInstance(
					Thread.currentThread().getContextClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							SourceReferenceRepository sourceReferenceRepository =
									repositoryFactory.produceRepository(OrientDbSourceReferenceRepository.class);

							if (sourceReferenceRepository != null) {
								PaginationInfo<ISourceReference> paginationInfo = sourceReferenceRepository.findByReference(entity.getId(), page, entriesPerPage, null); //TODO limit to access
								return paginationInfo.getEntities();
							} else throw new NullPointerException("NULL sourceReferenceRepository");
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
					Thread.currentThread().getContextClassLoader(),
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
					Thread.currentThread().getContextClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							FileRepository fileRepository =
									repositoryFactory.produceRepository(OrientDbFileRepository.class);

							return fileRepository.findByReference(entity.getId(), entity.getModelName().equals("File")); //TODO limit to access
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

	@Override
	public boolean delete(@Nullable T entity) {
		if (entity == null) return true;

		if (super.delete(entity)) {
			// delete connected edges
			//repositoryFactory.getDb().command(new OCommandSQL("delete edge where in = " + entity.getId() + " OR out = " + entity.getId())).execute();
			// this is already done in AbstractOrientDbRepository

			// delete source reference pointing to me, too
			repositoryFactory.getDb().command(new OCommandSQL("delete from SourceReference where reference = " + entity.getId())).execute();

			return true;
		}
		return false;
	}

	/**
	 * create a tag filter SQL string to add to queries for tags
	 * @param tags list of tag titles
	 * @param withSubTags include subtags as well?
	 * @param in direction in instead of out (default)
	 * @return SQL string part
	 */
	protected String buildTagFilterSQL(String[] tags, boolean withSubTags, boolean in) {
		//TODO test!

		// tags
		if (tags != null && tags.length > 0) {
			// create search for titles
			StringBuilder sb = new StringBuilder("title IN [ ");
			boolean first = true;
			for (String tag : tags) {
				if (first) first = false;
				else sb.append(',');
				sb.append('\'').append(OrientStringEscape.escapeOrientSql(tag)).append('\'');
			}
			sb.append(']');

			// with sub tags?
			if (withSubTags) {
				//TODO: add test
				// keeps tag ids
				Set<String> subTagIds = new HashSet<>();

				// get ids of tag
				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from Tag where " + sb.toString());
				List<ODocument> tagIdsDocs = db.command(query).execute();
				for (ODocument tagDoc : tagIdsDocs) {
					// traverse sub tags
					OSQLSynchQuery<ODocument> query2 = new OSQLSynchQuery<>("SELECT @rid FROM (traverse " + (in?"in":"out") + "('IsTagOf') from " + tagDoc.field("rid", String.class) + " MAXDEPTH 1 WHILE @class = 'Tag') WHERE @class = 'Tag'");
					List<ODocument> subTagDocs = db.command(query2).execute();
					for (ODocument subTagDoc : subTagDocs) // add ids to set
						subTagIds.add(subTagDoc.field("rid", String.class));
				}

				sb = new StringBuilder(" in('IsTagOf') IN [ ");
				first = true;
				for (String tagId : subTagIds) {
					if (first) first = false;
					else sb.append(',');
					sb.append(tagId);
				}

				return sb.append("] ").toString();
			} else { //"normal" search
				return " in('IsTagOf')." + sb.toString();
			}
		}

		return "";
	}
}
