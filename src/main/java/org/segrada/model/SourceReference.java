package org.segrada.model;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.base.AbstractColoredModel;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.model.prototype.SegradaAnnotatedEntity;

import javax.validation.constraints.NotNull;

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
 * Source reference model implementation
 */
public class SourceReference extends AbstractColoredModel implements ISourceReference {
	// we will just use the color from the colored model as per definition in the interface

	private static final long serialVersionUID = 1L;

	@NotNull(message = "error.notNull")
	private ISource source;

	@NotNull(message = "error.notNull")
	private SegradaAnnotatedEntity reference;

	private String referenceText = "";

	private String roleOfNode = "";

	@Override
	public ISource getSource() {
		return source;
	}

	@Override
	public void setSource(ISource source) {
		this.source = source;
	}

	@Override
	public SegradaAnnotatedEntity getReference() {
		return reference;
	}

	@Override
	public void setReference(SegradaAnnotatedEntity reference) {
		this.reference = reference;
	}

	@Override
	public String getReferenceText() {
		return referenceText;
	}

	@Override
	public void setReferenceText(String referenceText) {
		this.referenceText = referenceText;
	}

	@Override
	public String getRoleOfNode() {
		return roleOfNode;
	}

	@Override
	public void setRoleOfNode(String roleOfNode) {
		this.roleOfNode = roleOfNode;
	}

	@Override
	public String getTitle() {
		return getSource().getShortTitle() + (getReferenceText()==null||getReferenceText().isEmpty()?"":", " + getReferenceText());
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jsonObject = super.toJSON();

		try {
			jsonObject.put("source", source.getId());
			jsonObject.put("reference", reference.getId());
			jsonObject.put("referenceText", referenceText);
			jsonObject.put("roleOfNode", roleOfNode);
		} catch (Exception e) {
			// ignore
		}

		return jsonObject;
	}
}
