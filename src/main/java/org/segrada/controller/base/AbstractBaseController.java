package org.segrada.controller.base;

import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.ColorService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
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
abstract public class AbstractBaseController<BEAN extends SegradaEntity> {
	@Inject
	HttpSession session;

	/**
	 * Handle non paginated index
	 * @param service showing pages
	 * @param <E> type of repository
	 * @return view with list of entities set
	 */
	protected <E extends CRUDRepository<BEAN>> Viewable handleShowAll(AbstractRepositoryService<BEAN, E> service) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("entities", service.findAll());

		return new Viewable(getBasePath() + "index", model);
	}

	/**
	 * Handle paginated index
	 * @param service showing pages
	 * @param page page to show starting with 1
	 * @param entriesPerPage entries per page
	 * @param filters filter options
	 * @return view with paginationInfo set
	 */
	protected Viewable handlePaginatedIndex(PaginatingRepositoryOrService<BEAN> service, int page, int entriesPerPage, Map<String, Object> filters) {
		return handlePaginatedIndex(service, page, entriesPerPage, filters, null, null);
	}

	/**
	 * Handle paginated index - generic with custom view name
	 * @param service showing pages
	 * @param page page to show starting with 1
	 * @param entriesPerPage entries per page
	 * @param filters filter options
	 * @param viewName name of view, e.g. "index"
	 * @return view with paginationInfo set
	 */
	protected Viewable handlePaginatedIndex(PaginatingRepositoryOrService<BEAN> service, int page, int entriesPerPage, Map<String, Object> filters, @Nullable String viewName, @Nullable Map<String, Object> model) {
		// define default values
		if (viewName == null) viewName = "index";
		if (model == null) model = new HashMap<>();
		if (filters == null) filters = new HashMap<>();

		//  get or create session key for filter
		String key = filters.containsKey("key")?(String)filters.get("key"):service.getClass().getSimpleName();

		// reset all filters
		if (filters.containsKey("reset")) {
			Map<String, Object> newFilters = new HashMap<>();
			// keep certain values?
			if (filters.containsKey("resetKeep")) {
				for (String keepKey : (String[])filters.get("resetKeep")) {
					if (filters.containsKey(keepKey))
						newFilters.put(keepKey, filters.get(keepKey));
				}
			}
			filters = newFilters;
		}
		else {
			// get filter entries from session
			Object o = session.getAttribute(key);
			if (o != null && o instanceof Map) { // we have a session object saved - now copy filters
				Map<String, Object> sessionFilter = (Map<String, Object>) session.getAttribute(key);
				// copy filter keys that are in sessionFilter, but not in filters
				for (String filterKey : sessionFilter.keySet()) {
					if (!filters.containsKey(filterKey))
						filters.put(filterKey, sessionFilter.get(filterKey));
				}
			}
		}
		Map<String, Object> cleanedFilter = new HashMap<>();
		for (String filterKey : filters.keySet()) {
			Object o = filters.get(filterKey);
			if (o != null && !(o instanceof String && ((String)o).isEmpty())) cleanedFilter.put(filterKey, o);
		}
		// get page and per page settings
		if (page > 0) cleanedFilter.put("page", page); // set page
		else page = cleanedFilter.containsKey("page")?(int)cleanedFilter.get("page"):1; // set page from filter or initialize
		if (entriesPerPage > 0) cleanedFilter.put("entriesPerPage", entriesPerPage); // set entriesPerPage
		else entriesPerPage = cleanedFilter.containsKey("entriesPerPage")?(int)cleanedFilter.get("entriesPerPage"):15; // set entriesPerPage from filter or initialize

		// save to session
		session.setAttribute(key, cleanedFilter);

		// add to model map
		model.put("paginationInfo", service.paginate(page, entriesPerPage, cleanedFilter));
		model.put("filters", cleanedFilter);

		return new Viewable(getBasePath() + viewName, model);
	}

	/**
	 * Handle detail view
	 * @param uid of entity to show
	 * @param service showing entities
	 * @param <E> type of repository
	 * @return view with detail of entity
	 */
	protected <E extends CRUDRepository<BEAN>> Viewable handleShow(String uid, AbstractRepositoryService<BEAN, E> service) {
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
	protected Viewable handleForm(@Nullable SegradaEntity entity) {
		if (entity == null)
			return new Viewable("error", "Could not find entity in database.");

		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId()==null|| entity.getId().isEmpty());
		model.put("entity", entity);

		enrichModelForEditingAndSaving(model);

		return new Viewable(getBasePath() + "form", model);
	}

	/**
	 * handle whole update method of an entity
	 * @param entity to save
	 * @param service saving entity
	 * @param <E> type of repository
	 * @return response, either form view or redirect
	 */
	protected <E extends CRUDRepository<BEAN>> Response handleUpdate(BEAN entity, AbstractRepositoryService<BEAN, E> service) {
		// validate entity
		Map<String, String> errors = validate(entity);
		// extra validation
		validateExtra(errors, entity);

		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId() == null || entity.getId().isEmpty());

		// no validation errors: save entity
		if (errors.isEmpty()) {
			if (service.save(entity)) {
				clearCache(); // delete caches

				//OK - redirect to show
				try {
					return Response.seeOther(new URI(getBasePath() + "show/" + entity.getUid())).build();
				} catch (URISyntaxException e) {
					return Response.ok(new Viewable("error", e.getMessage())).build();
				}
			} else return Response.ok(new Viewable("error", "SAVE failed.")).build();
		}

		// fill model map
		model.put("entity", entity);
		model.put("errors", errors);

		enrichModelForEditingAndSaving(model);

		// return viewable
		return Response.ok(new Viewable(getBasePath() + "form", model)).build();
	}

	/**
	 * handle delete of an entity
	 * @param empty if this is empty, redirect to list view, otherwise return empty response
	 * @param entity to delete
	 * @param service deleting entity
	 * @param <E> type of repository
	 * @return response, either empty response or list
	 */
	protected <E extends CRUDRepository<BEAN>> Response handleDelete(String empty, BEAN entity, AbstractRepositoryService<BEAN, E> service) {
		boolean emptyValue = empty == null || empty.isEmpty() || empty.equals("0");

		if (!service.delete(entity)) {
			return Response.ok(new Viewable("error", "DELETE failed.")).build();
		}

		clearCache(); // delete caches

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
	 * @return map containing property and one error message
	 */
	protected Map<String, String> validate(BEAN entity) {
		Map<String, String> errors = new HashMap<>();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		for (ConstraintViolation<BEAN> error : validator.validate(entity)) {
			errors.put(error.getPropertyPath().toString(), error.getMessage());
		}

		return errors;
	}

	/**
	 * @return controller's base path, e.g. "/source/"
	 */
	abstract protected String getBasePath();

	/**
	 * extra validation hook
	 * @param errors error map
	 * @param entity to validate
	 */
	protected void validateExtra(Map<String, String> errors, BEAN entity) {
		//Do nothing by default
	}

	/**
	 * called to enrich the model for editing and saving - hook
	 * @param model to be enriched
	 */
	protected void enrichModelForEditingAndSaving(Map<String, Object> model) {
		//Do nothing by default
	}

	/**
	 * clear page caches
	 */
	protected void clearCache() {
		// delete caches
		Ehcache cache = CacheManager.getInstance().getEhcache("SimplePageCachingFilter");
		if (cache != null) {
			cache.removeAll(); // flush whole cache
		}
	}
}
