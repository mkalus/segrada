package org.segrada.model.prototype;

import org.codehaus.jettison.json.JSONObject;

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
 * Source reference model interface
 */
public interface ISourceReference extends SegradaEntity {
	ISource getSource();
	void setSource(ISource source);

	SegradaAnnotatedEntity getReference();
	void setReference(SegradaAnnotatedEntity reference);

	String getReferenceText();
	void setReferenceText(String referenceText);

	String getRoleOfNode();
	void setRoleOfNode(String roleOfNode);

	Integer getColor();
	void setColor(Integer color);

	/**
	 * get color as hex code
	 * @return color as hex code
	 */
	String getColorCode();
	void setColorCode(String colorCode);

	JSONObject toJSON();
}
