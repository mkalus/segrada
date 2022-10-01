package org.segrada.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationSettingsPropertiesTest {
	/**
	 * instance to test
	 */
	private ApplicationSettingsProperties applicationSettings;

	@BeforeEach
	public void setUp() throws Exception {
		// environmental variables cannot be tested but should be ok
		applicationSettings = new ApplicationSettingsProperties();
	}

	@Test
	public void testGetSetting() throws Exception {
		assertEquals("TEST", applicationSettings.getSetting("environment"));

		assertEquals("memory:segradatest", applicationSettings.getSetting("orientDB.url"));
	}

	@Test
	public void testGetSettingWithDefault() throws Exception {
		assertEquals("TEST", applicationSettings.getSetting("environment"));

		assertEquals("memory:segradatest", applicationSettings.getSetting("orientDB.url", "default"));
		assertEquals("default", applicationSettings.getSetting("orientDB.urlxx", "default"));
		assertNull(applicationSettings.getSetting("orientDB.urlxx", null));
	}

	@Test
	public void testSetSetting() throws Exception {
		assertNull(applicationSettings.getSetting("dummy"));

		applicationSettings.setSetting("dummy", "foobar");

		assertEquals("foobar", applicationSettings.getSetting("dummy"));
	}

	@Test
	public void testGetKeys() throws Exception {
		Collection<String> keys = applicationSettings.getKeys();

		assertNotNull(keys);
		assertTrue(keys.size() > 0);
		assertTrue(keys.contains("environment"));
	}
}
