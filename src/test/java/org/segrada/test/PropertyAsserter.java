package org.segrada.test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test utility class that makes easy work of testing default behavior of getters and setters.
 * <p>
 * Use:<br>
 * <pre>
 * PropertyAsserter.assertBasicGetterSetterBehavior(targetObject);
 * </pre>
 * <p>
 * Thread safety: not tested.
 *
 * @author Scott Leberknight
 * @author Josef Betancourt
 */
@SuppressWarnings("rawtypes")
public enum PropertyAsserter {
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(PropertyAsserter.class);
	private static final Map<Class, Object> TYPE_ARGUMENTS = new HashMap<Class, Object>();
	private static final double PIE_DOUBLE = 3.14159;
	private static final float PIE_FLOAT = 3.14159F;
	private static final long TEN_LONG = 10L;
	private static final int NUM_TEN = 10;

	static {
		TYPE_ARGUMENTS.put(Collection.class, new ArrayList());
		TYPE_ARGUMENTS.put(List.class, new ArrayList());
		TYPE_ARGUMENTS.put(Set.class, new HashSet());
		TYPE_ARGUMENTS.put(SortedSet.class, new TreeSet());
		TYPE_ARGUMENTS.put(Map.class, new HashMap());
		TYPE_ARGUMENTS.put(SortedMap.class, new TreeMap());
		TYPE_ARGUMENTS.put(Boolean.class, true);
		TYPE_ARGUMENTS.put(Boolean.TYPE, true);
		TYPE_ARGUMENTS.put(Character.class, 'Z');
		TYPE_ARGUMENTS.put(Character.TYPE, 'Z');
		TYPE_ARGUMENTS.put(Byte.class, (byte) NUM_TEN);
		TYPE_ARGUMENTS.put(Byte.TYPE, (byte) NUM_TEN);
		TYPE_ARGUMENTS.put(Short.class, (short) NUM_TEN);
		TYPE_ARGUMENTS.put(Short.TYPE, (short) NUM_TEN);
		TYPE_ARGUMENTS.put(Integer.class, NUM_TEN);
		TYPE_ARGUMENTS.put(Integer.TYPE, NUM_TEN);
		TYPE_ARGUMENTS.put(Long.class, TEN_LONG);
		TYPE_ARGUMENTS.put(Long.TYPE, TEN_LONG);
		TYPE_ARGUMENTS.put(Float.class, PIE_FLOAT);
		TYPE_ARGUMENTS.put(Float.TYPE, PIE_FLOAT);
		TYPE_ARGUMENTS.put(Double.class, PIE_DOUBLE);
		TYPE_ARGUMENTS.put(Double.TYPE, PIE_DOUBLE);
		TYPE_ARGUMENTS.put(java.sql.Date.class, new java.sql.Date(new Date().getTime()));
		TYPE_ARGUMENTS.put(Timestamp.class, new Timestamp(new Date().getTime()));
		TYPE_ARGUMENTS.put(Calendar.class, Calendar.getInstance());
	}

	private static final Map<Class, Object> DEFAULT_TYPE_ARGUMENTS = Collections.unmodifiableMap(new HashMap<Class, Object>(TYPE_ARGUMENTS));

	private static final int TEST_ARRAY_SIZE = NUM_TEN;

	/**
	 * Registers the specified type that will default to the specified <code>defaultArgument</code> as the argument to
	 * setter methods.
	 * <p>
	 * Note this method will override any existing default arguments for a type.
	 *
	 * @param type            the type to register
	 * @param defaultArgument the default argument to use in setters
	 */
	public static void registerTypeAndDefaultArgument(Class type, Object defaultArgument) {
		TYPE_ARGUMENTS.put(type, defaultArgument);
	}

	/**
	 * Removes the specified type, so that there wil no longer be a default argument for the type.
	 *
	 * @param type the type to deregister
	 */
	public static void deregisterType(Class type) {
		TYPE_ARGUMENTS.remove(type);
	}

	/** Resets the types and default arguments. */
	public static void resetToDefaultTypes() {
		TYPE_ARGUMENTS.clear();
		TYPE_ARGUMENTS.putAll(DEFAULT_TYPE_ARGUMENTS);
	}

	/**
	 * Returns the default argument for the specified type.
	 *
	 * @param type the type
	 * @return the type's default argument
	 */
	public static Object defaultArgumentForType(Class type) {
		return TYPE_ARGUMENTS.get(type);
	}

	/**
	 * Tests that the getter and setter methods for <code>property</code> work in a basic fashion.
	 * <p>
	 * Getter returns the exact same object as set by the setter method. (And we don't care that FindBugz says this is
	 * bad, bad, bad and furthermore we disable that check in FindBugz anyway based on the Reduction of Java
	 * Overengineering Act. Then again, some might argue that <i>this</i> class itself embodies Java Overengineering!)
	 * <p/> Uses a default argument for basic collection types, primitive types, Dates, java.sql.Dates, and Timestamps.
	 *
	 * See {@link PropertyAsserter#TYPE_ARGUMENTS}.
	 *
	 * @param target   the object on which to invoke the getter and setter
	 * @param property the property name, e.g. "firstName"
	 */
	public static void assertBasicGetterSetterBehavior(Object target, String property) {
		assertBasicGetterSetterBehavior(target, property, null);
	}

	/**
	 * Use an explicit argument to test getter/setter.
	 * <p>
	 * See {@link #assertBasicGetterSetterBehavior(Object,String)} method.
	 *
	 * @param target   the object on which to invoke the getter and setter
	 * @param property the property name, e.g. "firstName"
	 * @param argument the property value, i.e. the value the setter will be invoked with
	 */
	public static void assertBasicGetterSetterBehavior(Object target, String property, Object argument) {
		try {
			PropertyDescriptor descriptor = new PropertyDescriptor(property, target.getClass());
			Object arg = argument;
			Class type = descriptor.getPropertyType();
			if (arg == null) {
				if (type.isArray()) {
					arg = Array.newInstance(type.getComponentType(), new int[]{ TEST_ARRAY_SIZE });
				}else if (type.isEnum()) {
					arg = type.getEnumConstants()[0];
				}else if (TYPE_ARGUMENTS.containsKey(type)) {
					arg = TYPE_ARGUMENTS.get(type);
				}else {
					try {
						Constructor<?> constructor = findDefaultConstructor(type);
						if (constructor == null) {
							return;
						}

						arg = invokeDefaultConstructorEvenIfPrivate(type);
					} catch (Exception e) {
						arg = null;
					}

					if (arg == null) {
						return;
					}

				}
			}

			Method writeMethod = descriptor.getWriteMethod();
			Method readMethod = descriptor.getReadMethod();

			writeMethodInvoke(target, arg, writeMethod);
			Object propertyValue = readMethodInvoke(target, readMethod);
			assertPropertyResult(property, arg, type, propertyValue);
		}catch (IntrospectionException e) {
			String msg = "Error creating PropertyDescriptor for property [" + property
					+ "]. Do you have a getter and a setter?";
			log.error(msg, e);
			fail(msg);
		}catch (IllegalAccessException e) {
			String msg = "Error accessing property [" + property + "]. Are the getter and setter both accessible?";
			log.error(msg, e);
			fail(msg);
		}catch (InvocationTargetException e) {
			String msg = "Error invoking method on target";
			log.error(msg, e);
			fail(msg);
		}
	}

	/**
	 * The actual assertions on the property value.
	 * <p>
	 * Extracted to a method for future unit testing.
	 *
	 * @param property
	 * @param arg
	 * @param type
	 * @param propertyValue
	 */
	static void assertPropertyResult(String property, Object arg, Class type, Object propertyValue) {
		if (type.isPrimitive() || type == String.class) {
			assertEquals(property + " getter/setter failed test", arg, propertyValue);
		}else {
			assertSame(property + " getter/setter failed test", arg, propertyValue);
		}
	}

	/**
	 * Finds the constructor corresponding with the given parameter types.
	 * <p/>
	 *
	 * @param clazz what is the class type
	 * @param parameterTypes the parameters to check
	 * @return a constructor, if one can be found, else null
	 */
	public static Constructor<?> findDefaultConstructor(Class<?> clazz, Class<?>... parameterTypes) {
		try {
			final Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
			return constructor;
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Create object using constructor via reflection.
	 * <p>
	 * Set accessible is set true.  Note this may fail due to various reasons, such as security settings.
	 *
	 * @param type
	 * @return the object created
	 */
	private static Object invokeDefaultConstructorEvenIfPrivate(Class<?> type) {
		try {
			Constructor ctor = type.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		}catch (Exception ex) {
			throw new RuntimeException("Could not invoke default constructor on type " + type, ex);
		}
	}

	/**
	 * Use a map of property names/value for getter/setter testing.
	 * <p>
	 * See {@link #assertBasicGetterSetterBehavior(Object,String)} method. Only difference is that here we accept a map
	 * containing property name/value pairs. Use this to test a bunch of property accessors at once. Note that the
	 * values in the map can be null, and in that case we'll try to supply a default argument.
	 *
	 * @param target     the object on which to invoke the getter and setter
	 * @param properties map of property names to argument values
	 */
	public static void assertBasicGetterSetterBehavior(Object target, Map<String, Object> properties) {
		Set<Map.Entry<String, Object>> entries = properties.entrySet();

		for (Map.Entry<String, Object> entry : entries) {
			assertBasicGetterSetterBehavior(target, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Use array (variable length arguments) to specify which properties to test.
	 * <p>
	 * See {@link #assertBasicGetterSetterBehavior(Object,String)} method. Only difference is that here we accept an
	 * array of property names. Use this to test a bunch of property accessors at once, using default arguments.
	 *
	 * @param target        the object on which to invoke the getter and setter
	 * @param propertyNames the names of the propertyes you want to test
	 */
	public static void assertBasicGetterSetterBehavior(Object target, String... propertyNames) {
		Map<String, Object> properties = new LinkedHashMap<String, Object>();

		for (String propertyName : propertyNames) {
			properties.put(propertyName, null);
		}

		assertBasicGetterSetterBehavior(target, properties);
	}

	/**
	 * Test all Javabean properties in object.
	 * <p>
	 * See {@link #assertBasicGetterSetterBehavior(Object, String[])} method. No items are blacklisted.
	 *
	 * @param target the object on which to invoke the getter and setter
	 */
	public static void assertBasicGetterSetterBehavior(Object target) {
		assertBasicGetterSetterBehaviorWithBlacklist(target);
	}

	/**
	 * Use a String array (variable length arguments) to specify which properties to skip.
	 * <p>
	 * See {@link #assertBasicGetterSetterBehavior(Object,String)} method. Big difference here is that we try to
	 * automatically introspect the target object, finding read/write properties, and automatically testing the getter
	 * and setter. Note specifically that read-only properties are ignored, as there is no way for us to know how to set
	 * the value (since there isn't a public setter).
	 * <p/>
	 * Any property names contained in the blacklist will be skipped.
	 * <p/>
	 *
	 * @param target        the object on which to invoke the getter and setter
	 * @param propertyNames the list of property names that should not be tested
	 */
	public static void assertBasicGetterSetterBehaviorWithBlacklist(Object target, String... propertyNames) {
		List<String> blacklist = Arrays.asList(propertyNames);

		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

			for (PropertyDescriptor descriptor : descriptors) {
				if (descriptor.getWriteMethod() == null || (descriptor.getReadMethod() == null) ) {
					log.debug("property, '{}' does not have both getter and setter.", descriptor.getDisplayName());
					continue;
				}
				if (!blacklist.contains(descriptor.getDisplayName())) {
					assertBasicGetterSetterBehavior(target, descriptor.getDisplayName());
				}else {
					log.debug("skipping property: '{}'",descriptor.getDisplayName());
				}
			}
		}catch (IntrospectionException e) {
			fail("Failed while introspecting target " + target.getClass());
		}
	}

	/**
	 * @param target
	 * @param readMethod
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	static Object readMethodInvoke(Object target, Method readMethod) throws IllegalAccessException, IllegalArgumentException,InvocationTargetException {

		log.debug("Invoking read method {} on object: {}",readMethod,target.getClass());
		Object result = readMethod.invoke(target);
		log.debug("read result: '{}'", result);

		return result;
	}

	/**
	 * @param target
	 * @param arg
	 * @param writeMethod
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	static void writeMethodInvoke(Object target, Object arg,Method writeMethod) throws IllegalAccessException,IllegalArgumentException, InvocationTargetException {
		log.debug("writing method on object '{}' with arg: '{}'", target.getClass(),arg);
		writeMethod.invoke(target, arg);
		log.debug("Wrote arg: '{}'",arg);
	}

}