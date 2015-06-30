package org.segrada.model.base;

import org.junit.Test;
import org.segrada.model.User;

import static org.junit.Assert.*;

public class AbstractSegradaEntityTest {
	@Test
	public void testEquals() throws Exception {
		MockEntity entity1 = new MockEntity();
		MockEntity entity2 = new MockEntity();

		assertEquals(entity1, entity2);

		entity1.setCreated(1L);
		entity2.setCreated(2L);
		entity1.setModified(1L);
		entity2.setModified(2L);
		entity1.setCreator(new User());
		entity2.setCreator(new User());
		entity1.setModifier(new User());
		entity2.setModifier(new User());

		assertEquals(entity1, entity2);

		// now add load - should not be equal!
		entity1.setDummyLoad("Test1");
		entity2.setDummyLoad("Test2");

		assertNotEquals(entity1, entity2);

		// test for null
		assertFalse(entity1.equals(null));
	}

	@Test
	public void testHashCode() throws Exception {
		MockEntity entity1 = new MockEntity();
		MockEntity entity2 = new MockEntity();

		assertEquals(entity1.hashCode(), entity2.hashCode());

		entity1.setCreated(1L);
		entity2.setCreated(2L);
		entity1.setModified(1L);
		entity2.setModified(2L);
		entity1.setCreator(new User());
		entity2.setCreator(new User());
		entity1.setModifier(new User());
		entity2.setModifier(new User());

		assertEquals(entity1.hashCode(), entity2.hashCode());

		// now add load - should not be equal!
		entity1.setDummyLoad("Test1");
		entity2.setDummyLoad("Test2");

		assertNotEquals(entity1.hashCode(), entity2.hashCode());
	}

	@Test
	public void testGetModelName() throws Exception {
		MockEntity entity = new MockEntity();

		assertEquals("MockEntity", entity.getModelName());
	}

	@Test
	public void testToString() throws Exception {
		MockEntity entity = new MockEntity();
		assertEquals("{MockEntity}*, DUMMY", entity.toString());

		entity.setId("ID");
		assertEquals("{MockEntity}ID, DUMMY", entity.toString());

		entity.setId(null);
		assertEquals("{MockEntity}*, DUMMY", entity.toString());

		entity.setDummyLoad("XYZ");
		assertEquals("{MockEntity}*, XYZ", entity.toString());
	}

	@Test
	public void testGetUid() throws Exception {
		MockEntity entity = new MockEntity();
		assertEquals("", entity.getUid());

		entity.setId("#12345:45678");

		assertEquals("Id to Uid conversion failed", "12345-45678", entity.getUid());
	}

	private class MockEntity extends AbstractSegradaEntity {
		/**
		 * load for testing equality
		 */
		private String dummyLoad = "DUMMY";

		@Override
		public String getTitle() {
			return dummyLoad;
		}

		public String getDummyLoad() {
			return dummyLoad;
		}

		public void setDummyLoad(String dummyLoad) {
			this.dummyLoad = dummyLoad;
		}
	}
}