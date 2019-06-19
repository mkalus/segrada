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
 * Source model interface
 */
public interface ISource extends SegradaAnnotatedEntity {
	String getShortTitle();
	void setShortTitle(String shortTitle);

	String getLongTitle();
	void setLongTitle(String shortTitle);

	String getShortRef();
	void setShortRef(String shortRef);

	String getUrl();
	void setUrl(String url);

	String getProductCode();
	void setProductCode(String productCode);

	String getAuthor();
	void setAuthor(String author);

	String getCitation();
	void setCitation(String citation);

	String getCopyright();
	void setCopyright(String copyright);

	String getDescription();
	void setDescription(String description);

	String getDescriptionMarkup();
	void setDescriptionMarkup(String descriptionMarkup);
}
