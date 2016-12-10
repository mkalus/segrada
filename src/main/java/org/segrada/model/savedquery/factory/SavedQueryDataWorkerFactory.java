package org.segrada.model.savedquery.factory;

import com.google.inject.Inject;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.model.savedquery.SavedQueryDataWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * Factory for saved query data workers
 */
public class SavedQueryDataWorkerFactory {
	private static final Logger logger = LoggerFactory.getLogger(SavedQueryDataWorkerFactory.class);

	@Inject
	private Map<String, SavedQueryDataWorker> validators;

	/**
	 * factory method to produce new saved query data validators
	 * @param dataType to produce for
	 * @return SavedQueryDataWorker or null
	 */
	public SavedQueryDataWorker produceSavedQueryDataValidator(String dataType) {
		if (dataType == null) return null;

		// get validator
		SavedQueryDataWorker validator = validators.get(dataType.toLowerCase());
		if (validator != null) return validator;

		// testing environment
		if (dataType.equals("mock")) {
			return new SavedQueryDataWorker() {
				@Override
				public boolean validateData(String data) {
					if (data == null) return false;
					return true;
				}

				@Override
				public Map<String, List<SegradaEntity>> savedQueryToEntities(String data) {
					return null;
				}


			};
		}

		logger.error("Error while producing saved query data validator type " + dataType);
		return null;
	}
}
