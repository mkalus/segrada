package org.segrada.service.util;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class AbstractLazyLoadedObjectTest {
	private MockObject mockObject;
	private MockAbstractLazyLoadedObject mockAbstractLazyLoadedObject;

	@Before
	public void setUp() throws Exception {
		mockObject = new MockObject();
		mockAbstractLazyLoadedObject = new MockAbstractLazyLoadedObject(mockObject);
	}

	@Test
	public void testLoadObject() throws Exception {
		assertEquals(mockObject, mockAbstractLazyLoadedObject.loadObject());
	}

	@Test
	public void testInvokeTest() throws Exception {
		Method test = IMockObject.class.getMethod("test");
		try {
			Object result = mockAbstractLazyLoadedObject.invoke(IMockObject.class, test, null);
			// should return true
			assertEquals(true, result);
		} catch (Throwable t) {
			throw new Exception(t);
		}
	}

	@Test
	public void testInvokeTestWithParameters() throws Exception {
		Method testWithParameters = IMockObject.class.getMethod("testWithParameters", String.class);
		try {
			Object result = mockAbstractLazyLoadedObject.invoke(IMockObject.class, testWithParameters, new Object[]{ "TEST" });
			assertEquals("_TEST", result);
		} catch (Throwable t) {
			throw new Exception(t);
		}
	}

	/**
	 * Mock interface to lazy load
	 */
	private interface IMockObject {
		public boolean test();

		public String testWithParameters(String p);
	}

	/**
	 * Mock object to lazy load
	 */
	private class MockObject implements IMockObject {
		public boolean test() {
			return true;
		}

		public String testWithParameters(String p) {
			return "_" + p;
		}
	}

	/**
	 * Partial mock class to test
	 */
	private class MockAbstractLazyLoadedObject extends AbstractLazyLoadedObject {
		/**
		 * dummy reference to instance
		 */
		private final MockObject mockObject;

		public MockAbstractLazyLoadedObject(MockObject mockObject) {
			this.mockObject = mockObject;
		}

		@Override
		protected Object loadObject() {
			return mockObject;
		}
	}
}