package org.segrada.model;

import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.model.prototype.SegradaAnnotatedEntity;

import javax.validation.constraints.NotNull;

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
 * Source reference model implementation
 */
public class SourceReference extends AbstractSegradaEntity implements ISourceReference {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "error.notNull")
	private ISource source;

	@NotNull(message = "error.notNull")
	private SegradaAnnotatedEntity reference;

	private String referenceText = "";

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
	public String getTitle() {
		return getSource().getShortTitle() + (getReferenceText()==null||getReferenceText().isEmpty()?"":", " + getReferenceText());
	}
}
