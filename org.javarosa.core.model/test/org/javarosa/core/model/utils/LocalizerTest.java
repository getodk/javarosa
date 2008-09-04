package org.javarosa.core.model.utils;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.NoSuchElementException;

public class LocalizerTest extends TestCase  {
	public final int NUM_TESTS = 29;
	
	public LocalizerTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public LocalizerTest(String name) {
		super(name);
	}

	public LocalizerTest() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new LocalizerTest("Localizer Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((LocalizerTest)tc).testMaster(testID);
				}
			}));
		}

		return aSuite;
	}
	
	public void testMaster (int testID) {
		//System.out.println("running " + testID);
		
		switch (testID) {
		case 1: testEmpty(); break;
		case 2: testAddLocale(); break;
		case 3: testAddLocaleWithData(); break;
		case 4: testAddExistingLocale(); break;
		case 5: testAddExistingLocaleOverwrite(); break;
		case 6: testSetCurrentLocaleExists(); break;
		case 7: testSetCurrentLocaleNotExists(); break;
		case 8: testUnsetCurrentLocale(); break;
		case 9: testSetDefaultLocaleExists(); break;
		case 10: testSetDefaultLocaleNotExists(); break;
		case 11: testUnsetDefaultLocale(); break;
		case 12: testSetToDefault(); break;
		case 13: testSetToDefaultNoDefault(); break;
		case 14: testDestroyLocale(); break;
		case 15: testDestroyLocaleNotExist(); break;
		case 16: testDestroyCurrentLocale(); break;
		case 17: testDestroyDefaultLocale(); break;
		case 18: testAvailableLocales(); break;
		case 19: testGetNextLocale(); break;
		case 20: testGetLocaleMap(); break;
		case 21: testGetLocaleMapNotExist(); break;
		case 22: testTextMapping(); break;
		case 23: testTextMappingRemove(); break;
		case 24: testTextMappingOverwrite(); break;
		case 25: testGetText(); break;
		case 26: testGetTextNoCurrentLocale(); break;
		case 27: testLocalizationObservers(); break;
		case 28: testLocalizationObserverUpdateOnRegister(); break;
		case 29: testNullArgs(); break;
		}
	}
	
	public void testEmpty () {
		Localizer l = new Localizer();
		
		String[] locales = l.getAvailableLocales();
		if (locales == null || locales.length > 0) {
			fail("New localizer not empty");
		}		
		String currentLocale = l.getLocale();
		if (currentLocale != null) {
			fail("New localizer has locale set");
		}
		String defaultLocale = l.getDefaultLocale();
		if (defaultLocale != null) {
			fail("New localizer has default locale set");
		}
	}
	
	public void testAddLocale () {
		Localizer l = new Localizer();	
		final String TEST_LOCALE = "test";
		
		if (l.hasLocale(TEST_LOCALE)) {
			fail("Localizer reports it contains non-existent locale");
		}
		boolean result = l.addAvailableLocale(TEST_LOCALE);
		if (!result) {
			fail("Localizer failed to add new locale");
		}
		if (!l.hasLocale(TEST_LOCALE)) {
			fail("Localizer reports it does not contain newly added locale");
		}
		SimpleOrderedHashtable localeData = l.getLocaleData(TEST_LOCALE);
		if (localeData == null || localeData.size() != 0) {
			fail("Newly created locale not empty (or undefined)");
		}
	}
	
	public void testAddLocaleWithData () {
		Localizer l = new Localizer();	
		final String TEST_LOCALE = "test";
		SimpleOrderedHashtable localeData = new SimpleOrderedHashtable();
		localeData.put("textID", "text");
		
		if (l.hasLocale(TEST_LOCALE)) {
			fail("Localizer reports it contains non-existent locale");
		}
		boolean result = l.setLocaleData(TEST_LOCALE, localeData);
		if (result) {
			fail("Overwrote a non-existent locale");
		}
		if (!l.hasLocale(TEST_LOCALE)) {
			fail("Localizer reports it does not contain newly added locale");
		}
		if (localeData != l.getLocaleData(TEST_LOCALE)) {
			fail("Newly stored locale does not match source");
		}	
	}
	
	public void testAddExistingLocale () {
		Localizer l = new Localizer();	
		final String TEST_LOCALE = "test";

		l.addAvailableLocale(TEST_LOCALE);
		l.setLocaleMapping(TEST_LOCALE, "textID", "text");
		SimpleOrderedHashtable localeData = l.getLocaleData(TEST_LOCALE);
		
		boolean result = l.addAvailableLocale(TEST_LOCALE);
		if (result) {
			fail("Localizer overwrote existing locale");
		}
		if (localeData != l.getLocaleData(TEST_LOCALE)) {
			fail("Localizer overwrote existing locale");			
		}
	}
		
	public void testAddExistingLocaleOverwrite () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";

		l.addAvailableLocale(TEST_LOCALE);
		l.setLocaleMapping(TEST_LOCALE, "oldTextID", "oldText");
		
		SimpleOrderedHashtable localeData = new SimpleOrderedHashtable();
		localeData.put("newTextID", "newText");
		
		boolean result = l.setLocaleData(TEST_LOCALE, localeData);
		if (!result) {
			fail("Localizer did not overwrite locale as expected");
		}
		if (localeData != l.getLocaleData(TEST_LOCALE)) {
			fail("Newly overwritten locale does not match source");			
		}
	}
	
	public void testSetCurrentLocaleExists () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		l.addAvailableLocale(TEST_LOCALE);
		
		l.setLocale(TEST_LOCALE);
		if (!TEST_LOCALE.equals(l.getLocale())) {
			fail("Did not properly set current locale");
		}
	}
	
	public void testSetCurrentLocaleNotExists () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		
		try {
			l.setLocale(TEST_LOCALE);
			
			fail("Set current locale to a non-existent locale");
		} catch (NoSuchElementException nsee) {
			//expected
		}
	}
	
	public void testUnsetCurrentLocale () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		l.addAvailableLocale(TEST_LOCALE);
		l.setLocale(TEST_LOCALE);
		
		try {
			l.setLocale(null);
			
			fail("Able to unset current locale");
		} catch (NoSuchElementException nsee) {
			//expected
		}
	}
	
	public void testSetDefaultLocaleExists () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		l.addAvailableLocale(TEST_LOCALE);
		
		l.setDefaultLocale(TEST_LOCALE);
		if (!TEST_LOCALE.equals(l.getDefaultLocale())) {
			fail("Did not properly set default locale");
		}
	}
	
	public void testSetDefaultLocaleNotExists () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		
		try {
			l.setDefaultLocale(TEST_LOCALE);
			
			fail("Set current locale to a non-existent locale");
		} catch (NoSuchElementException nsee) {
			//expected
		}
	}

	public void testUnsetDefaultLocale () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		l.addAvailableLocale(TEST_LOCALE);
		l.setDefaultLocale(TEST_LOCALE);
		
		try {
			l.setDefaultLocale(null);
			
			if (l.getDefaultLocale() != null) {
				fail("Could not unset default locale");
			}
		} catch (NoSuchElementException nsee) {
			fail("Exception unsetting default locale");
		}
	}
	
	public void testSetToDefault () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		l.addAvailableLocale(TEST_LOCALE);
		l.setDefaultLocale(TEST_LOCALE);
		
		l.setToDefault();
		if (!TEST_LOCALE.equals(l.getLocale())) {
			fail("Could not set current locale to default");
		}
	}

	public void testSetToDefaultNoDefault () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";
		l.addAvailableLocale(TEST_LOCALE);

		try {
			l.setToDefault();

			fail("Set current locale to default when no default set");
		} catch (IllegalStateException ise) {
			//expected
		}
	}

	public void testDestroyLocale () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";		
		l.addAvailableLocale(TEST_LOCALE);
		
		boolean result = l.destroyLocale(TEST_LOCALE);
		if (!result || l.hasLocale(TEST_LOCALE)) {
			fail("Locale not destroyed");
		}
	}

	public void testDestroyLocaleNotExist () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";		
		
		boolean result = l.destroyLocale(TEST_LOCALE);
		if (result) {
			fail("Destroyed non-existent locale");
		}
	}

	public void testDestroyCurrentLocale () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";		
		l.addAvailableLocale(TEST_LOCALE);
		l.setLocale(TEST_LOCALE);
		
		try {
			l.destroyLocale(TEST_LOCALE);

			fail("Destroyed current locale");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	public void testDestroyDefaultLocale () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";		
		l.addAvailableLocale(TEST_LOCALE);
		l.setDefaultLocale(TEST_LOCALE);
		
		l.destroyLocale(TEST_LOCALE);
		if (l.getDefaultLocale() != null) {
			fail("Default locale still set to destroyed locale");
		}
	}

	public void testAvailableLocales () {
		Localizer l = new Localizer();
		String[] locales;
		
		l.addAvailableLocale("test1");
		locales = l.getAvailableLocales();
		if (locales.length != 1 || !locales[0].equals("test1")) {
			fail("Available locales not as expected");
		}
		
		l.addAvailableLocale("test2");
		locales = l.getAvailableLocales();
		if (locales.length != 2 || !locales[0].equals("test1") || !locales[1].equals("test2")) {
			fail("Available locales not as expected");
		}

		l.addAvailableLocale("test3");
		locales = l.getAvailableLocales();
		if (locales.length != 3 || !locales[0].equals("test1") || !locales[1].equals("test2") || !locales[2].equals("test3")) {
			fail("Available locales not as expected");
		}

		l.destroyLocale("test2");
		locales = l.getAvailableLocales();
		if (locales.length != 2 || !locales[0].equals("test1") || !locales[1].equals("test3")) {
			fail("Available locales not as expected");
		}
		l.destroyLocale("test1");
		locales = l.getAvailableLocales();
		if (locales.length != 1 || !locales[0].equals("test3")) {
			fail("Available locales not as expected");
		}
		l.destroyLocale("test3");
		locales = l.getAvailableLocales();
		if (locales == null || locales.length != 0) {
			fail("Available locales not as expected");
		}	
	}
	
	public void testGetNextLocale () {
		Localizer l = new Localizer();
		l.addAvailableLocale("test1");
		l.addAvailableLocale("test2");
		l.addAvailableLocale("test3");
		
		if (l.getNextLocale() != null) {
			fail("Unexpected next locale");
		}
		
		l.setDefaultLocale("test3");
		if (!"test3".equals(l.getNextLocale())) {
			fail("Unexpected next locale");
		}

		l.setDefaultLocale(null);
		l.setLocale("test1");
		if (!"test2".equals(l.getNextLocale())) {
			fail("Unexpected next locale");
		}
		l.setLocale("test2");
		if (!"test3".equals(l.getNextLocale())) {
			fail("Unexpected next locale");
		}
		l.setLocale("test3");
		if (!"test1".equals(l.getNextLocale())) {
			fail("Unexpected next locale");
		}
	}
	
	public void testGetLocaleMap () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";		
		l.addAvailableLocale(TEST_LOCALE);

		if (l.getLocaleMap(TEST_LOCALE) != l.getLocaleData(TEST_LOCALE)) {
			fail();
		}
	}
	
	public void testGetLocaleMapNotExist () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";		

		try {
			l.getLocaleMap(TEST_LOCALE);
			
			fail("Did not throw exception when getting locale mapping for non-existent locale");
		} catch (NoSuchElementException nsee) {
			//expected
		}
	}
	
	public void testTextMapping () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";	
		l.addAvailableLocale(TEST_LOCALE);
		
		if (l.hasMapping(TEST_LOCALE, "textID")) {
			fail("Localizer contains text mapping that was not defined");
		}
		l.setLocaleMapping(TEST_LOCALE, "textID", "text");
		if (!l.hasMapping(TEST_LOCALE, "textID")) {
			fail("Localizer does not contain newly added text mapping");
		}
		if (!"text".equals((String)l.getLocaleData(TEST_LOCALE).get("textID"))) {
			fail("Newly added text mapping does not match source");
		}
	}
	
	public void testTextMappingRemove () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";	
		l.addAvailableLocale(TEST_LOCALE);
		l.setLocaleMapping(TEST_LOCALE, "textID", "text");
		
		l.setLocaleMapping(TEST_LOCALE, "textID", null);
		if (l.hasMapping(TEST_LOCALE, "textID")) {
			fail("Text mapping not removed");
		}
	}

	public void testTextMappingOverwrite () {
		Localizer l = new Localizer();
		final String TEST_LOCALE = "test";	
		l.addAvailableLocale(TEST_LOCALE);
		l.setLocaleMapping(TEST_LOCALE, "textID", "oldText");
		
		l.setLocaleMapping(TEST_LOCALE, "textID", "newText");
		if (!l.hasMapping(TEST_LOCALE, "textID")) {
			fail("Localizer does not contain overwritten text mapping");
		}
		if (!"newText".equals((String)l.getLocaleData(TEST_LOCALE).get("textID"))) {
			fail("Newly overwritten text mapping does not match source");
		}
	}
	
	public void testGetText () {
		for (int localeCase = 1; localeCase <= 3; localeCase++) {
			for (int formCase = 1; formCase <= 2; formCase++) {
				testGetText(localeCase, formCase);
			}
		}
	}
	
	private static final int DEFAULT_LOCALE = 1;
	private static final int NON_DEFAULT_LOCALE = 2;
	private static final int NEUTRAL_LOCALE = 3;

	private static final int BASE_FORM = 1;
	private static final int CUSTOM_FORM = 2;
		
	public void testGetText (int localeCase, int formCase) {
		String ourLocale = null;
		String otherLocale = null;
		
		switch (localeCase) {
		case DEFAULT_LOCALE: ourLocale = "default"; otherLocale = null; break;
		case NON_DEFAULT_LOCALE: ourLocale = "other"; otherLocale = "default"; break;
		case NEUTRAL_LOCALE: ourLocale = "neutral"; otherLocale = null; break;
		}
		
		String textID = "textID" + (formCase == CUSTOM_FORM ? ";form" : "");
		
		for (int i = 0; i < 4; i++) { //iterate through 4 possible fallback modes
			for (int j = 0; j < 4; j++) {
				if (otherLocale == null) {
					testGetText(i, j, -1, ourLocale, otherLocale, textID, localeCase, formCase);
				} else {
					for (int k = 0; k < 4; k++) {
						testGetText(i, j, k, ourLocale, otherLocale, textID, localeCase, formCase);
					}
				}
			}
		}
	}
	
	public void testGetText (int i, int j, int k, String ourLocale, String otherLocale, String textID, int localeCase, int formCase) {
		//System.out.println("testing getText: "+localeCase+","+formCase+","+i+","+j+","+k);
		
		Localizer l = buildLocalizer(i, j, k, ourLocale, otherLocale);
		String expected = expectedText(textID, l);
		String text, text2;
		
		text = l.getText(textID, ourLocale);
		if (expected == null ? text != null : !expected.equals(text)) {
			fail("Did not retrieve expected text from localizer ["+localeCase+","+formCase+","+i+","+j+","+k+"]");
		}
		
		try {
			text2 = l.getLocalizedText(textID);
			
			if (expected == null) {
				fail("Should have gotten exception");
			} else if (!expected.equals(text2)) {
				fail("Did not retrieve expected text");
			}
		} catch (NoSuchElementException nsee) {
			if (expected != null) {
				fail("Got unexpected exception");
			}
		}
	}
	
	private Localizer buildLocalizer (int i, int j, int k, String ourLocale, String otherLocale) {
		Localizer l = new Localizer(i / 2 == 0, i % 2 == 0);
		l.addAvailableLocale(ourLocale);
		l.setLocale(ourLocale);
		if (otherLocale != null)
			l.addAvailableLocale(otherLocale);
		if (l.hasLocale("default"))
			l.setDefaultLocale("default");
		
		if (j / 2 == 0)
			l.setLocaleMapping(ourLocale, "textID", "text:" + ourLocale + ":base");
		if (j % 2 == 0)
			l.setLocaleMapping(ourLocale, "textID;form", "text:" + ourLocale + ":form");
			
		if (otherLocale != null) {
			if (k / 2 == 0)
				l.setLocaleMapping(otherLocale, "textID", "text:" + otherLocale + ":base");
			if (k % 2 == 0)
				l.setLocaleMapping(otherLocale, "textID;form", "text:" + otherLocale + ":form");	
		}
		
		return l;	
	}
	
	private String expectedText (String textID, Localizer l) {
		boolean[] searchOrder = new boolean[4];
		boolean fallbackLocale = l.getFallbackLocale();
		boolean fallbackForm = l.getFallbackForm();
		boolean hasForm = (textID.indexOf(";") != -1);
		boolean hasDefault = (l.getDefaultLocale() != null && !l.getDefaultLocale().equals(l.getLocale()));
		String baseTextID = (hasForm ? textID.substring(0, textID.indexOf(";")) : textID);
		
		searchOrder[0] = hasForm;
		searchOrder[1] = !hasForm || fallbackForm;
		searchOrder[2] = hasForm && (hasDefault && fallbackLocale);
		searchOrder[3] = (!hasForm || fallbackForm) && (hasDefault && fallbackLocale);
				
		String text = null;
		for (int i = 0; text == null && i < 4; i++) {
			if (!searchOrder[i])
				continue;
			
			switch (i + 1) {
			case 1: text = l.getRawText(l.getLocale(), textID); break;
			case 2: text = l.getRawText(l.getLocale(), baseTextID); break;
			case 3: text = l.getRawText(l.getDefaultLocale(), textID); break;
			case 4: text = l.getRawText(l.getDefaultLocale(), baseTextID); break;
			}
		}
		
		return text;
	}
	
	public void testGetTextNoCurrentLocale () {
		Localizer l = new Localizer();
		l.addAvailableLocale("test");
		l.setDefaultLocale("test");
		l.setLocaleMapping("test", "textID", "text");
		
		try {
			String text = l.getText("textID");
			
			fail("Retrieved current locale text when current locale not set");
		} catch (NoSuchElementException nsee) {
			//expected
		}
	}
	
	private class LocalizationObserver implements Localizable {
		public boolean flag = false;
		public String locale;
		public Localizer l;
		
		public void localeChanged (String locale, Localizer l) {
			flag = true;
			this.locale = locale;
			this.l = l;
		}
	}
	
	public void testLocalizationObservers () {
		Localizer l = new Localizer();
		l.addAvailableLocale("test1");
		l.addAvailableLocale("test2");
		LocalizationObserver lo = new LocalizationObserver();
		l.registerLocalizable(lo);
		
		if (lo.flag || lo.locale != null || lo.l != null) {
			fail("Improper state in localization observer");
		}
		
		l.setLocale("test1");
		if (!lo.flag || !"test1".equals(lo.locale) || l != lo.l) {
			fail("Improper state in localization observer, or not updated properly");
		}
		lo.flag = false;
		
		l.setLocale("test2");
		if (!lo.flag || !"test2".equals(lo.locale) || l != lo.l) {
			fail("Improper state in localization observer, or not updated properly");
		}
		lo.flag = false;
		
		l.setLocale("test2");
		if (lo.flag || !"test2".equals(lo.locale) || l != lo.l) {
			fail("Localization observer improperly updated");
		}
		
		l.unregisterLocalizable(lo);
		l.setLocale("test1");
		if (lo.flag || !"test2".equals(lo.locale) || l != lo.l) {
			fail("Localization observer updated after unregistered");
		}
	}
	
	public void testLocalizationObserverUpdateOnRegister () {
		Localizer l = new Localizer();
		l.addAvailableLocale("test1");
		l.setLocale("test1");
		
		LocalizationObserver lo = new LocalizationObserver();
		l.registerLocalizable(lo);
		
		if (!lo.flag || !"test1".equals(lo.locale) || l != lo.l) {
			fail("Localization observer did not update properly on registration");
		}
	}
	
	public void testNullArgs () {
		Localizer l = new Localizer();
		l.addAvailableLocale("test");
		
		try {
			l.addAvailableLocale(null);
			
			fail("addAvailableLocale: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}
		
		if (l.hasLocale(null)) {
			fail("Localizer reports it contains null locale");
		}
		
		try {
			l.setLocaleData(null, new SimpleOrderedHashtable());
			
			fail("setLocaleData: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}
		
		try {
			l.setLocaleData("test", null);
			
			fail("setLocaleData: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}
		
		if (l.getLocaleData(null) != null) {
			fail("getLocaleData: Localizer returns mappings for null locale");
		}	
		
		try {
			l.getLocaleMap(null);
			
			fail("getLocaleMap: Did not get expected exception");
		} catch (NoSuchElementException nsee) {
			//expected
		}		
		
		try {
			l.setLocaleMapping(null, "textID", "text");
			
			fail("setLocaleMapping: Did not get expected exception");
		} catch (NoSuchElementException nsee) {
			//expected
		}		
		
		try {
			l.setLocaleMapping("test", null, "text");
			
			fail("setLocaleMapping: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}		
		
		try {
			l.setLocaleMapping("test", null, null);
			
			fail("setLocaleMapping: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}			
		
		try {
			l.hasMapping(null, "textID");
			
			fail("hasMapping: Did not get expected exception");
		} catch (NoSuchElementException nsee) {
			//expected
		}		
		
		if (l.hasMapping("test", null)) {
			fail("Localization reports it contains null mapping");
		}	
		
		try {
			l.destroyLocale(null);
			
			fail("destroyLocale: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}	
		
		try {
			l.getText("textID", null);
			
			fail("getText: Did not get expected exception");
		} catch (NoSuchElementException nsee) {
			//expected
		}		
		
		try {
			l.getText(null, "test");
			
			fail("getText: Did not get expected null pointer exception");
		} catch (NullPointerException npe) {
			//expected
		}
	}
	
	//test serialization
}
