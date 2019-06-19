package org.segrada.model.prototype;

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
 * Relation model interface
 */
public interface IRelation extends SegradaCoreEntity {
	IRelationType getRelationType();
	void setRelationType(IRelationType relationType);

	String getDescription();
	void setDescription(String description);

	String getDescriptionMarkup();
	void setDescriptionMarkup(String descriptionMarkup);

	INode getFromEntity();
	void setFromEntity(INode fromEntity);

	INode getToEntity();
	void setToEntity(INode toEntity);

	/**
	 * Get reversed title
	 * @return reversed title
	 */
	String getReversedTitle();
}
