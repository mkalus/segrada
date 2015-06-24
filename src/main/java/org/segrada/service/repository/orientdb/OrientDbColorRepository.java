package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.Color;
import org.segrada.model.prototype.IColor;
import org.segrada.service.repository.ColorRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;

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
 * OrientDb Color Repository
 */
public class OrientDbColorRepository extends AbstractSegradaOrientDbRepository<IColor> implements ColorRepository {
	/**
	 * Constructor
	 */
	@Inject
	public OrientDbColorRepository(ODatabaseDocumentTx db, ApplicationSettings applicationSettings, Identity identity) {
		super(db, applicationSettings, identity);
	}

	@Override
	public String getModelClassName() {
		return "Color";
	}

	@Override
	public IColor convertToEntity(ODocument document) {
		Color color = new Color();

		color.setTitle(document.field("title", String.class));
		color.setColor(document.field("color", Integer.class));

		// populate with data
		populateEntityWithBaseData(document, color);
		populateEntityWithCreatedModified(document, color);

		return color;
	}

	@Override
	public ODocument convertToDocument(IColor entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("title", entity.getTitle())
				.field("color", entity.getColor());

		// populate with data
		populateODocumentWithCreatedModified(document, (Color) entity);

		return document;
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" color");
	}
}
