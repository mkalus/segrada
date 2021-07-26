package org.segrada.controller.base;

import com.google.inject.Inject;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.ColorService;

import java.util.Map;

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
 * Abstract colored base controller - injects services for colors and pictograms in controller
 */
public abstract class AbstractColoredController<T extends SegradaEntity> extends AbstractBaseController<T> {
	@Inject
	protected ColorService colorService;

	@Override
	protected void enrichModelForEditingAndSaving(Map<String, Object> model) {
		super.enrichModelForEditingAndSaving(model);

		// add colors
		model.put("colors", colorService.findAll());
	}
}
