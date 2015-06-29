package org.segrada.servlet;

import org.apache.commons.lang.WordUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.tika.io.IOUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.segrada.model.prototype.SegradaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	private static final Logger logger = LoggerFactory.getLogger(SegradaMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public SegradaEntity readFrom(Class<SegradaEntity> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
		// create instance
		SegradaEntity entity;
		try {
			Object o = aClass.newInstance();
			if (!(o instanceof SegradaEntity))
				return null; //TODO: make strings work and the like
			entity = (SegradaEntity) o;
		} catch (Exception e) {
			logger.error("Could not convert to entity: " + aClass, e);
			return null;
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

		// read form data
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		String theString = writer.toString();

		// get each form key and value
		for (NameValuePair nameValuePair : URLEncodedUtils.parse(theString, Charset.forName("UTF-8"))) {
			Method setter = setters.get(nameValuePair.getName());
			if (setter != null) {
				// get first parameter of setter (type to cast)
				Class setterType = setter.getParameterTypes()[0];

				// TODO: handle more complex setters, e.g. objects

				// try to set value
				try {
					// invoke setter on entity
					setter.invoke(entity, setterType.cast(nameValuePair.getValue()));
				} catch (Exception e) {
					logger.error("Could not set entity of type " + aClass.getSimpleName() + ", method " + setter.getName() + " to value " + nameValuePair.getValue() + " (type " + setterType.getName() + ")", e);
				}
			}
		}

		return entity;
	}
}
