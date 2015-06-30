package org.segrada.controller.base;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
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
 * Abstract base controller
 */
abstract public class AbstractBaseController {
	/**
	 * validate bean
	 * @param entity to validate
	 * @param <T> type of bean
	 * @return map containing property and one error message
	 */
	protected <T> Map<String, String> validate(T entity) {
		Map<String, String> errors = new HashMap<>();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		for (ConstraintViolation<T> error : validator.validate(entity)) {
			errors.put(error.getPropertyPath().toString(), error.getMessage());
		}

		return errors;
	}
}
