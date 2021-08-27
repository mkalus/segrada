package org.segrada.service.binarydata;

import com.google.common.io.Files;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.segrada.model.Node;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.session.ApplicationSettings;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BinaryDataServiceFileTest {
	private static String savePath;

	private static File repositoryPath;

	private static File tempPath;

	@BeforeClass
	public static void setUpClass() throws Exception {
		savePath = System.getProperty("java.io.tmpdir") + File.separator + "segradatest";


		repositoryPath = new File(savePath, "binary");

		// create dirs
		tempPath = new File(savePath);
		tempPath.mkdirs();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		// delete all
		deleteDirectory(tempPath);
	}

	/**
	 * helper to recurively delete directory and contents
	 * @param directory to delete
	 * @return true, if delete was ok
	 */
	public static boolean deleteDirectory(File directory) {
		if(directory.exists()){
			File[] files = directory.listFiles();
			if(null!=files){
				for (File file : files) {
					if (file.isDirectory()) {
						deleteDirectory(file);
					} else {
						file.delete();
					}
				}
			}
		}
		return(directory.delete());
	}

	/**
	 * instance to test
	 */
	private BinaryDataServiceFile binaryDataServiceFile;

	@Before
	public void setUp() throws Exception {
		binaryDataServiceFile = new BinaryDataServiceFile(new ApplicationSettings() {
			@Override
			public String getSetting(String key) {
				return savePath;
			}

			@org.jetbrains.annotations.Nullable
			@Override
			public String getSettingOrDefault(String key, String defaultValue) {
				return null;
			}

			@Override
			public int getSettingAsInteger(String key, int defaultValue) {
				return 0;
			}

			@Override
			public double getSettingAsDouble(String key, double defaultValue) {
				return 0;
			}

			@Override
			public Map<String, String> getAllSettingsStartingWith(String key) {
				return null;
			}

			@Nullable
			@Override
			public String getSetting(String key, @Nullable String defaultValue) {
				return savePath;
			}

			@Override
			public void setSetting(String key, String newValue) {

			}

			@Override
			public Collection<String> getKeys() {
				return null;
			}
		});
	}

	@Test
	public void testReferenceExists() throws Exception {
		String id = "testExists.txt";

		// create temporary file in path
		File test = new File(repositoryPath, id);
		test.createNewFile();

		assertTrue(binaryDataServiceFile.referenceExists(id));
	}

	@Test
	public void testRemoveReference() throws Exception {
		String id = "testRemove.txt";

		// create temporary file in path
		File test = new File(repositoryPath, id);
		test.createNewFile();

		assertTrue(binaryDataServiceFile.removeReference(id));
		assertFalse(test.exists());
	}

	@Test
	public void testSaveNewReference() throws Exception {
		String id = "testNew.txt";

		byte[] data = "HELLO\nWORLD!".getBytes();

		// create dummy entity
		SegradaEntity entity = new AbstractSegradaEntity() {
			@Override
			public String getId() {
				return "#99:01";
			}

			@Override
			public String getTitle() {
				return "DUMMY";
			}
		};

		// create reference
		String newId = binaryDataServiceFile.saveNewReference(entity, id, "text/plain", data, null);

		File test = new File(repositoryPath, newId);
		assertTrue(test.exists());
		assertEquals("HELLO", Files.readFirstLine(test, Charset.defaultCharset()));

		// metadata?
		File metadata = new File(repositoryPath, newId + ".metadata");
		assertTrue(metadata.exists());
		assertEquals(id, Files.readFirstLine(metadata, Charset.defaultCharset()));

		// replace reference?
		String replacedId = binaryDataServiceFile.saveNewReference(entity, id, "text/plain", data, newId);
		assertNotEquals(newId, replacedId);
		File replacedTest = new File(repositoryPath, replacedId);
		assertTrue(replacedTest.exists());
		// old file should be deleted
		assertFalse(test.exists());
	}

	@Test
	public void testUpdateReferenceId() throws Exception {
		String id = "testNew.txt";

		byte[] data = "HELLO\nWORLD!".getBytes();

		// create dummy entity
		Node node = new Node();

		// create reference
		String newId = binaryDataServiceFile.saveNewReference(node, id, "text/plain", data, null);
		// metadata?
		File metadata = new File(repositoryPath, newId + ".metadata");
		List<String> lines = Files.readLines(metadata, Charset.defaultCharset());

		assertEquals("Node:null", lines.get(1));

		// change id and update
		node.setId("#99:02");
		binaryDataServiceFile.updateReferenceId(newId, node);

		lines = Files.readLines(metadata, Charset.defaultCharset());

		assertEquals("Node:#99:02", lines.get(1));
	}

	@Test
	public void testGetBinaryData() throws Exception {
		String id = "testGetBinary.txt";

		File test = new File(repositoryPath, id);

		byte[] data = "HELLO\nWORLD!".getBytes();

		// write to file
		Files.write(data, test);

		assertArrayEquals(data, binaryDataServiceFile.getBinaryData(id));
	}

	@Test
	public void testGetBinaryDataAsStream() throws Exception {
		String id = "testGetBinary.txt";

		File test = new File(repositoryPath, id);

		byte[] data = "HELLO\nWORLD!".getBytes();

		// write to file
		Files.write(data, test);

		InputStream is = binaryDataServiceFile.getBinaryDataAsStream(id);

		byte[] check = new byte[is.available()];
		is.read(check);

		assertArrayEquals(data, check);
	}

	@Test
	public void testGetFilename() throws Exception {
		String id = "testOriginalFilename.txt";

		byte[] data = "HELLO\nWORLD!".getBytes();

		// create dummy entity
		SegradaEntity entity = new AbstractSegradaEntity() {
			@Override
			public String getId() {
				return "#99:01";
			}

			@Override
			public String getTitle() {
				return "DUMMY";
			}
		};

		// create reference
		String newId = binaryDataServiceFile.saveNewReference(entity, id, "text/plain", data, null);

		assertEquals(id, binaryDataServiceFile.getFilename(newId));
	}
}