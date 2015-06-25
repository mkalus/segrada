package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.SourceReference;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.service.repository.SourceReferenceRepository;
import org.segrada.service.repository.SourceRepository;
import org.segrada.service.repository.orientdb.base.AbstractOrientDbRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
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
 * OrientDb Source Reference Repository
 */
public class OrientDbSourceReferenceRepository extends AbstractSegradaOrientDbRepository<ISourceReference> implements SourceReferenceRepository {
	private static final Logger logger = LoggerFactory.getLogger(OrientDbSourceReferenceRepository.class);

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbSourceReferenceRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "SourceReference";
	}

	@Override
	public ISourceReference convertToEntity(ODocument document) {
		SourceReference sourceReference = new SourceReference();

		// set from/to
		ORecordId source = document.field("source", ORecordId.class);
		if (source != null) {
			SourceRepository sourceRepository = repositoryFactory.produceRepository(OrientDbSourceRepository.class);
			if (sourceRepository != null)
				sourceReference.setSource(sourceRepository.find(source.getIdentity().toString()));
			else logger.warn("Could not produce class OrientDbSourceRepository while converting to entity.");
		}
		Object referenceField = document.field("reference");
		if (referenceField != null) {
			ODocument reference = null;
			if (referenceField instanceof ODocument) reference = (ODocument) referenceField;
			else if (referenceField instanceof ORecordId) {
				reference = repositoryFactory.getDb().load((ORecordId) referenceField);
			} else {
				logger.error("Invalid class type: " + referenceField.getClass().getName());
			}

			if (reference != null) {
				AbstractOrientDbRepository dynamicRepository = (AbstractOrientDbRepository) repositoryFactory.produceRepository(reference.getClassName());
				if (dynamicRepository != null)
					sourceReference.setReference((SegradaAnnotatedEntity) dynamicRepository.convertToEntity(reference));
				else logger.warn("Could not produce class for document " + reference.toString() + " while converting to entity.");
			}
		}

		// rest is easy
		sourceReference.setReferenceText(document.field("referenceText"));
		populateEntityWithBaseData(document, sourceReference);

		// populate with data
		populateEntityWithCreatedModified(document, sourceReference);

		return sourceReference;
	}

	@Override
	public ODocument convertToDocument(ISourceReference entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("referenceText", entity.getReferenceText())
				.field("source", new ORecordId(entity.getSource().getId()))
				.field("reference", new ORecordId(entity.getReference().getId()));

		// populate with data
		populateODocumentWithCreatedModified(document, (SourceReference) entity);

		return document;
	}

	@Override
	public List<ISourceReference> findBySource(String id) {
		return findByX(id, "source");
	}

	@Override
	public List<ISourceReference> findByReference(String id) {
		return findByX(id, "reference");
	}

	/**
	 * helper function for both methods above
	 * @param id of entity
	 * @param direction either "in" or "out"
	 * @return list of source references found
	 */
	private List<ISourceReference> findByX(String id, String direction) {
		List<ISourceReference> list = new LinkedList<>();

		// empty?
		if (id == null) return list;

		initDb();

		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from SourceReference where " + direction + " = ?");
		List<ODocument> result = db.command(query).execute(new ORecordId(id));

		for (ODocument document : result) {
			list.add(convertToEntity(document));
		}

		return list;
	}
}
