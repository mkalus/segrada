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
 * Relation type model interface
 */
public interface IRelationType extends SegradaColoredEntity, SegradaTaggable {
	String getFromTitle();
	void setFromTitle(String fromTitle);

	String getToTitle();
	void setToTitle(String toTitle);

	String[] getFromTags();
	void setFromTags(String[] fromTags);

	String[] getToTags();
	void setToTags(String[] toTags);

	/**
	 * Helpers to create comma separated list of tag ids
	 */
	String getFromTagIds();
	String getToTagIds();

	String getDescription();
	void setDescription(String description);

	String getDescriptionMarkup();
	void setDescriptionMarkup(String descriptionMarkup);
}
