package org.segrada.model.base;

import org.junit.Test;
import org.segrada.model.Comment;
import org.segrada.model.File;
import org.segrada.model.SourceReference;
import org.segrada.model.User;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.ISourceReference;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractAnnotatedModelTest {

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
		entity1.setTags(new String[]{});
		entity2.setTags(new String[]{"Test"});

		List<IFile> files1 = new ArrayList<>();
		List<IFile> files2 = new ArrayList<>();
		files1.add(new File());
		List<IComment> comments1 = new ArrayList<>();
		List<IComment> comments2 = new ArrayList<>();
		comments1.add(new Comment());
		List<ISourceReference> sourceReferences1 = new ArrayList<>();
		List<ISourceReference> sourceReferences2 = new ArrayList<>();
		sourceReferences1.add(new SourceReference());

		entity1.setFiles(files1);
		entity2.setFiles(files2);
		entity1.setComments(comments1);
		entity2.setComments(comments2);
		entity1.setSourceReferences(sourceReferences1);
		entity2.setSourceReferences(sourceReferences2);

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
		entity1.setTags(new String[]{});
		entity2.setTags(new String[] { "Test"});

		List<IFile> files1 = new ArrayList<>();
		List<IFile> files2 = new ArrayList<>();
		files1.add(new File());
		List<IComment> comments1 = new ArrayList<>();
		List<IComment> comments2 = new ArrayList<>();
		comments1.add(new Comment());
		List<ISourceReference> sourceReferences1 = new ArrayList<>();
		List<ISourceReference> sourceReferences2 = new ArrayList<>();
		sourceReferences1.add(new SourceReference());

		entity1.setFiles(files1);
		entity2.setFiles(files2);
		entity1.setComments(comments1);
		entity2.setComments(comments2);
		entity1.setSourceReferences(sourceReferences1);
		entity2.setSourceReferences(sourceReferences2);

		assertEquals(entity1.hashCode(), entity2.hashCode());

		// now add load - should not be equal!
		entity1.setDummyLoad("Test1");
		entity2.setDummyLoad("Test2");

		assertNotEquals(entity1.hashCode(), entity2.hashCode());
	}

	private class MockEntity extends AbstractAnnotatedModel {
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