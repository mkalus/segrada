package org.segrada.service;

import com.google.inject.Inject;
import org.codehaus.jettison.json.JSONArray;
import org.segrada.model.SavedQuery;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.SavedQueryRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

import javax.annotation.Nullable;
import java.io.OutputStream;
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
 * Saved Query service
 */
public class SavedQueryService extends AbstractRepositoryService<ISavedQuery, SavedQueryRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public SavedQueryService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, SavedQueryRepository.class);
	}

	@Override
	public ISavedQuery createNewInstance() {
		return new SavedQuery();
	}

	@Override
	public Class<ISavedQuery> getModelClass() {
		return ISavedQuery.class;
	}

	/**
	 * Find list of saved query by filter
	 * @param user owner of saved query (null for all)
	 * @param type to find (null for all)
	 * @param title to find (null for all)
	 * @return list of saved queries found (or empty list if none)
	 */
	public List<ISavedQuery> findAllBy(@Nullable IUser user, @Nullable String type, @Nullable String title) {
		return repository.findAllBy(user, type, title);
	}

	/**
	 * run the saved query
	 * @param query saved query to run
	 * @return list of entities
	 */
	public List<SegradaEntity> runSavedQueryAndEntities(ISavedQuery query) {
		return repository.runSavedQueryAndEntities(query);
	}

	/**
	 * run the saved query and return JSON
	 * @param query saved query to run
	 * @return JSON structure of entitites found
	 */
	public JSONArray runSavedQueryAndGetJSONArray(ISavedQuery query) {
		return repository.runSavedQueryAndGetJSONArray(query);
	}

	/**
	 * run the saved query and return XML (GEXF)
	 * @param os stream to save XML to
	 * @param query saved query to run
	 */
	public void runSavedQueryAndGetXML(OutputStream os, ISavedQuery query) {
		repository.runSavedQueryAndGetXML(os, query);
	}

	/**
	 * run the saved query and return CSV
	 * @param os stream to save CSV to
	 * @param query saved query to run
	 * @return CSV string
	 */
	public void runSavedQueryAndGetCSV(OutputStream os, ISavedQuery query) {
		repository.runSavedQueryAndGetCSV(os, query);
	}
}
