package org.segrada.model.prototype;

import java.io.Serializable;

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
 * Base for all segrada entities
 */
public interface SegradaEntity extends Serializable {
	/**
	 * set entity id
	 * @param id of entity
	 */
	void setId(String id);
	String getId();

	/**
	 * @return id of entity in web secure format
	 */
	String getUid();

	/**
	 * set entity version
	 * @param version of entity
	 */
	void setVersion(int version);
	int getVersion();

	/**
	 * @return name of model
	 */
	String getModelName();

	/**
	 * @return title of entity to print in lists
	 */
	String getTitle();

	/**
	 * @return get creation date
	 */
	Long getCreated();
	void setCreated(Long created);

	/**
	 * @return get modification date
	 */
	Long getModified();
	void setModified(Long created);

	/**
	 * @return get creator or null
	 */
	IUser getCreator();
	void setCreator(IUser creator);

	/**
	 * @return get modifier or null
	 */
	IUser getModifier();
	void setModifier(IUser modifier);
}
