package org.segrada.controller.base;

import com.sun.jersey.api.view.Viewable;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
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
	 * Handle paginated index
	 * @param service showing pages
	 * @param page page to show starting with 1
	 * @param entriesPerPage entries per page
	 * @param filters filter options
	 * @param <T> type of entity
	 * @return view with paginationInfo set
	 */
	protected <T extends SegradaEntity> Viewable handlePaginatedIndex(PaginatingRepositoryOrService<T> service, int page, int entriesPerPage, Map<String, Object> filters) {
		// define default values
		if (page < 1) page = 1;
		if (entriesPerPage < 1) entriesPerPage = 5;

		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("paginationInfo", service.paginate(page, entriesPerPage, filters));

		return new Viewable(getBasePath() + "index", model);
	}

	/**
	 * Handle detail view
	 * @param uid of entity to show
	 * @param service showing entities
	 * @param <T> type of entity
	 * @param <E> type of repository
	 * @return view with detail of entity
	 */
	protected <T extends SegradaEntity, E extends CRUDRepository<T>> Viewable handleShow(String uid, AbstractRepositoryService<T, E> service) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("entity", service.findById(service.convertUidToId(uid)));

		return new Viewable(getBasePath() + "show", model);
	}

	/**
	 * Handle add or edit form
	 * @param entity to be created or edited (controller has to create form)
	 * @return view containing form
	 */
	protected Viewable handleForm(SegradaEntity entity) {
		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId()==null);
		model.put("entity", entity);

		return new Viewable("source/form", model);
	}

	/**
	 * handle whole update method of an entity
	 * @param entity to save
	 * @param service saving entity
	 * @param <T> type of entity
	 * @param <E> type of repository
	 * @return response, either form view or redirect
	 */
	protected <T extends SegradaEntity, E extends CRUDRepository<T>> Response handleUpdate(T entity, AbstractRepositoryService<T, E> service) {
		// validate source
		Map<String, String> errors = validate(entity);

		// no validation errors: save entity
		if (errors.isEmpty()) {
			if (service.save(entity)) {
				//OK - redirect to show
				try {
					return Response.seeOther(new URI(getBasePath() + "show/" + entity.getUid())).build();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} else ;//TODO show error message?
		}

		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("entity", entity);
		model.put("errors", errors);

		// return viewable
		return Response.ok(new Viewable(getBasePath() + "form", model)).build();
	}

	/**
	 * handle delete of an entity
	 * @param empty if this is empty, redirect to list view, otherwise return empty response
	 * @param entity to delete
	 * @param service deleting entity
	 * @param <T> type of entity
	 * @param <E> type of repository
	 * @return response, either empty response or list
	 */
	protected <T extends SegradaEntity, E extends CRUDRepository<T>> Response handleDelete(String empty, T entity, AbstractRepositoryService<T, E> service) {
		boolean emptyValue = empty == null || empty.isEmpty() || empty.equals("0");

		if (!service.delete(entity)) {
			//TODO: show error?
		}

		// empty response
		if (!emptyValue)
			return Response.ok().build();

		//OK - redirect to show
		try {
			return Response.seeOther(new URI(getBasePath())).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

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

	/**
	 * @return controller's base path, e.g. "/source/"
	 */
	abstract protected String getBasePath();
}
