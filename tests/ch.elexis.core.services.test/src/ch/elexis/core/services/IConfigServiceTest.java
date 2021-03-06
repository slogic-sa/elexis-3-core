package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IConfigServiceTest extends AbstractServiceTest {

	private IConfigService configService = OsgiServiceUtil.getService(IConfigService.class).get();

	@Test
	public void getSetUserconfig() {
		IPerson person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson", LocalDate.now(),
				Gender.FEMALE).mandator().buildAndSave();
		IPerson person2 = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson2", "TestPerson2", LocalDate.now(),
				Gender.FEMALE).mandator().buildAndSave();

		assertTrue(configService.set(person, "key", "value"));
		assertTrue(configService.set(person2, "key", "value2"));

		assertEquals("value", configService.get(person, "key", null));
		assertEquals("value2", configService.get(person2, "key", null));

		assertTrue(configService.set(person, "key", null));
		assertNull(configService.get(person, "key", null));
		assertFalse(configService.set(person, "key", null));
	}

	@Test
	public void getSetConfig() {
		assertTrue(configService.set("key", "value"));
		assertEquals("value", configService.get("key", null));
		assertTrue(configService.set("key", null));
		assertFalse(configService.set("key", null));
	}

	@Test
	public void getSetAsList() {
		String TEST_KEY_SET = "TestKeySet";
		List<String> values = Arrays.asList(new String[] { "TestValue", "TestValue2", "TestValue3" });
		configService.set(TEST_KEY_SET, null);
		configService.setFromList(TEST_KEY_SET, values);
		List<String> asSet = configService.getAsList(TEST_KEY_SET, Collections.emptyList());
		assertEquals(3, asSet.size());
		assertTrue(asSet.contains("TestValue"));
		assertTrue(asSet.contains("TestValue2"));
		assertTrue(asSet.contains("TestValue3"));
	}
	
	@Test
	public void getSetBoolean() {
		configService.set("keyBoolA", "1");
		configService.set("keyBoolB", "true");
		configService.set("keyBoolC", "bla");
		configService.set("keyBoolD", "0");
		assertTrue(configService.get("keyBoolA", false));
		assertTrue(configService.get("keyBoolB", false));
		assertFalse(configService.get("keyBoolC", true));
		assertFalse(configService.get("keyBoolD", true));
	}

}
