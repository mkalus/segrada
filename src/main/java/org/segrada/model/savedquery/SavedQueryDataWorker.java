package org.segrada.model.savedquery;

import org.segrada.model.prototype.SegradaEntity;

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
 * Interface for data working of saved queries
 */
public interface SavedQueryDataWorker {
	/**
	 * validate data
	 * @param data representation to validate
	 * @return true if valid
	 */
	boolean validateData(String data);

	/**
	 * retrieve a list of entities from saved query
	 * @param data representation to extract
	 * @return map of lists, e.g. "nodes" -> list of nodes in graph + "edges" -> list of relations
	 */
	Map<String, Iterable<SegradaEntity>> savedQueryToEntities(String data);
}
