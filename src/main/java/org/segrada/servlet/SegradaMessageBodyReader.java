package org.segrada.servlet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.tika.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.base.AbstractRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class SegradaMessageBodyReader implements MessageBodyReader<SegradaEntity> {
	/**
	 * get injector - not very nice, but works
	 */
	@Inject
	private Injector injector;

	private static final Logger logger = LoggerFactory.getLogger(SegradaMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
		return SegradaEntity.class.isAssignableFrom(aClass);
	}

	@Override
	public SegradaEntity readFrom(Class<SegradaEntity> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
		HttpServletRequest request = injector.getInstance(HttpServletRequest.class);

		// read form data
		Map<String, String[]> parameterMap = request.getParameterMap();

		// find id
		String id = parameterMap.containsKey("id")?parameterMap.get("id")[0]:null;

		// create instance
		SegradaEntity entity;
		// load instance from repository?
		if (id != null) {
			AbstractRepositoryService service = getServiceForClassName(aClass.getSimpleName());
			if (service == null) {
				logger.error("No service for: " + aClass);
				return null;
			}
			entity = service.findById(id);
			if (entity == null) {
				logger.error("Could not load id " + id + " as entity: " + aClass);
				return null;
			}
			if (logger.isDebugEnabled())
				logger.debug("Loaded existing entity for update: " + entity.toString());
		} else {
			try {
				entity = aClass.newInstance();
			} catch (Exception e) {
				logger.error("Could not convert to entity: " + aClass, e);
				return null;
			}
			if (logger.isDebugEnabled())
				logger.debug("Loaded new entity for creation: " + entity.toString());
		}

		// map setter methods to value map
		Map<String, Method> setters = new HashMap<>();
		for (Method method : aClass.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("set")) {
				methodName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
				setters.put(methodName, method);
			}
		}

		// create key set
		Set<String> keys = parameterMap.keySet();
		// clear tags before setting new ones: make sure to clear tags if empty
		if (keys.contains("clearTags")) {
			Method setter = setters.get(parameterMap.get("clearTags")[0]);
			if (setter != null) {
				try {
					// setTags method?
					if (setter.getName().equals("setTags"))
						((SegradaTaggable)entity).setTags(new String[]{});
					else if (setter.getName().equals("setChildTags"))
						((ITag)entity).setChildTags(new String[]{});
				} catch (Exception e) {
					logger.error("Could not set entity of type " + aClass.getSimpleName() + ", method " + setter.getName() + " to clear its tags", e);
				}
			}
		}

		// get each form key and value
		for (Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
			// do not update id, ignore clearTags and _csrf
			if (parameterEntry.getKey().equals("id") || parameterEntry.getKey().equals("clearTags") || parameterEntry.getKey().equals("_csrf")) continue;

			Method setter = setters.get(parameterEntry.getKey());
			if (setter != null) {
				// get first parameter of setter (type to cast)
				Class setterType = setter.getParameterTypes()[0];

				// preprocess values
				String[] values = parameterEntry.getValue();
				// short cut first value - used in most cases
				String firstValue = values!=null&&values.length>0?values[0]:null;
				// object representation of value
				Object value = firstValue;

				// preprocess values
				if (value != null && !firstValue.isEmpty() && parameterEntry.getKey().equals("color") && firstValue.startsWith("#")) {
					try {
						if (value.equals("#ffffffff")) value = null;
						else value = Integer.decode(firstValue);
					} catch (NumberFormatException e) {
						value = null;
						// fail silently
					}
				}
				// handle segrada entities
				if (SegradaEntity.class.isAssignableFrom(setterType)) {
					if (value != null && !((String)value).isEmpty()) {
						try { // dynamically determine service and fetch element by id
							AbstractRepositoryService service = getServiceForClassName(setterType.getSimpleName());
							if (service == null) throw new Exception("Empty service.");
							value = service.findById((String) value);
						} catch (Exception e) {
							logger.error("Error getting/setting SegradaEntity reference of type " + setterType + ", value: " + value, e);
						}
					} else value = null; // reset in order to avoid warnings
				}
				// handle arrays
				if (String[].class.isAssignableFrom(setterType)) {
					value = values;
				}
				// handle booleans
				if (Boolean.class.isAssignableFrom(setterType)) {
					if (values != null && values.length > 1) {
						// multiple values -> test for "1" or true and the like
						value = false;
						for (String v : values)
							if (v != null && (v.equals("1") || v.equalsIgnoreCase("t") || v.equalsIgnoreCase("true"))) {
								value = true;
								break;
							}
					} // only one value -> take that
					else value = !(firstValue == null || firstValue.isEmpty() || firstValue.equals("0") || firstValue.equalsIgnoreCase("f") || firstValue.equalsIgnoreCase("false"));
				}

				// try to set value
				try {
					// invoke setter on entity
					if (value == null) setter.invoke(entity, value);
					else setter.invoke(entity, setterType.cast(value));
				} catch (Exception e) {
					logger.error("Could not set entity of type " + aClass.getSimpleName() + ", method " + setter.getName() + " to value " + value + " (type " + setterType.getName() + ")", e);
				}
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("Updated data of entity: " + entity.toString());

		return entity;
	}

	/**
	 * get service for (simple) class name
	 * @param className simple class name of model, e.g. Node or INode
	 * @return service or null
	 */
	private @Nullable AbstractRepositoryService getServiceForClassName(String className) {
		try {
			if (className.startsWith("I")) className = className.substring(1);

			Class serviceClass = Class.forName("org.segrada.service." + className + "Service");
			return (AbstractRepositoryService) injector.getInstance(serviceClass);
		} catch (Exception e) {
			logger.error("Error retrieving service for class " + className, e);
		}
		return null;
	}
}
