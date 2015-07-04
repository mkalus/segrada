package org.segrada.servlet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.tika.io.IOUtils;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.base.AbstractRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
		// read form data
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		String theString = writer.toString();

		// parse form data to key/value pairs
		List<NameValuePair> parameters = URLEncodedUtils.parse(theString, Charset.forName("UTF-8"));
		String id = null;
		// find id
		for (NameValuePair nameValuePair : parameters) {
			if (nameValuePair.getName().equals("id")) {
				id = nameValuePair.getValue();
				break;
			}
		}

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

		// get each form key and value
		for (NameValuePair nameValuePair : parameters) {
			// do not update id
			if (nameValuePair.getName().equals("id")) continue;

			Method setter = setters.get(nameValuePair.getName());
			if (setter != null) {
				// get first parameter of setter (type to cast)
				Class setterType = setter.getParameterTypes()[0];

				// preprocess values
				Object value = nameValuePair.getValue();
				if (value != null && !nameValuePair.getValue().isEmpty()) {
					if (nameValuePair.getName().equals("color") && nameValuePair.getValue().startsWith("#")) {
						try {
							if (nameValuePair.getValue().equals("#ffffffff")) value = null;
							else value = Integer.decode(nameValuePair.getValue());
						} catch (NumberFormatException e) {
							value = null;
							// fail silently
						}
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
					// get current property value
					String getterName = "g" + setter.getName().substring(1);
					try {
						Method getter = entity.getClass().getMethod(getterName);
						String[] currentValues = (String[]) getter.invoke(entity);
						if (currentValues == null) {
							value = new String[] { (String) value };
						} else {
							// add to string
							String[] newValues = new String[currentValues.length+1];
							for (int i = 0; i < currentValues.length; i++)
								newValues[i] = currentValues[i];
							newValues[currentValues.length] = (String)value;
							value = newValues;
						}
					} catch (Exception e) {
						logger.error("Could not get getter for " + aClass.getSimpleName() + ", method " + setter.getName() + " in order to assign array values", e);
						value = null;
					}
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
