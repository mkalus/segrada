package org.segrada.service.repository;

import org.segrada.model.prototype.INode;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.repository.prototype.SearchTermRepository;

import javax.annotation.Nullable;
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
 * Node Repository
 */
public interface NodeRepository extends CRUDRepository<INode>, SearchTermRepository<INode>, PaginatingRepositoryOrService<INode> {
	/**
	 * find by search terms, but also contain by tags (used in reference search)
	 * @param term to search for
	 * @param maximum to show
	 * @param returnWithoutTerm show list even without search
	 * @param tagIds list of tag ids to contain search in
	 * @return list of entities
	 */
	List<INode> findBySearchTermAndTags(@Nullable String term, int maximum, boolean returnWithoutTerm, @Nullable String[] tagIds);
}
