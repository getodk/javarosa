/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.utils.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.locale.TableLocaleSource;
import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.core.util.OrderedMap;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.test.ExternalizableTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class LocalizerTest extends TestCase  {
    private static final Logger logger = LoggerFactory.getLogger(LocalizerTest.class);

    public LocalizerTest(String name) {
        super(name);
        logger.info("Running {} test: {}...", this.getClass().getName(), name);
    }

    public static Test suite() {
        TestSuite aSuite = new TestSuite();

        for (int i = 1; i <= NUM_TESTS; i++) {
            final int testID = i;

            aSuite.addTest(new LocalizerTest(testMaster(testID)));
        }

        return aSuite;
    }

    public static final int NUM_TESTS = 31;

    public static String testMaster (int testID) {
        switch (testID) {
        case 1: return "testEmpty";
        case 2: return "testAddLocale";
        case 3: return "testAddLocaleWithData";
        case 4: return "testAddExistingLocale";
        case 5: return "testSetCurrentLocaleExists";
        case 6: return "testSetCurrentLocaleNotExists";
        case 7: return "testUnsetCurrentLocale";
        case 8: return "testSetDefaultLocaleExists";
        case 9: return "testSetDefaultLocaleNotExists";
        case 10: return "testUnsetDefaultLocale";
        case 11: return "testSetToDefault";
        case 12: return "testSetToDefaultNoDefault";
        case 13: return "testDestroyLocale";
        case 14: return "testDestroyLocaleNotExist";
        case 15: return "testDestroyCurrentLocale";
        case 16: return "testDestroyDefaultLocale";
        case 17: return "testAvailableLocales";
        case 18: return "testGetNextLocale";
        case 19: return "testGetLocaleMap";
        case 20: return "testGetLocaleMapNotExist";
        case 21: return "testTextMapping";
        case 22: return "testTextMappingOverwrite";
        case 23: return "testGetText";
        case 24: return "testGetTextNoCurrentLocale";
        case 25: return "testLocalizationObservers";
        case 26: return "testLocalizationObserverUpdateOnRegister";
        case 27: return "testNullArgs";
        case 28: return "testSerialization";
        case 29: return "testLinearSub";
        case 30: return "testHashSub";
        case 31: return "testFallbacks";

        }
        throw new IllegalStateException("Unexpected index");
    }

    private void testSerialize (Localizer l, String msg) {
        PrototypeFactory pf = new PrototypeFactory();
        pf.addClass(TableLocaleSource.class);
        ExternalizableTest.testExternalizable(l, this, pf, "Localizer [" + msg + "]");
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
        OrderedMap<String,String> localeData = l.getLocaleData(TEST_LOCALE);
        if (localeData == null || localeData.size() != 0) {
            fail("Newly created locale not empty (or undefined)");
        }
    }

    public void testAddLocaleWithData () {
        Localizer l = new Localizer();
        final String TEST_LOCALE = "test";
        TableLocaleSource localeData = new TableLocaleSource();
        localeData.setLocaleMapping("textID", "text");



        if (l.hasLocale(TEST_LOCALE)) {
            fail("Localizer reports it contains non-existent locale");
        }

        l.addAvailableLocale(TEST_LOCALE);
        l.registerLocaleResource(TEST_LOCALE, localeData);

        if (!l.hasLocale(TEST_LOCALE)) {
            fail("Localizer reports it does not contain newly added locale");
        }
        if (!localeData.getLocalizedText().get("textID").equals(l.getRawText(TEST_LOCALE,"textID"))) {
            fail("Newly stored locale does not match source");
        }
    }

    public void testAddExistingLocale () {
        Localizer l = new Localizer();
        final String TEST_LOCALE = "test";

        l.addAvailableLocale(TEST_LOCALE);
        TableLocaleSource table = new TableLocaleSource();
        table.setLocaleMapping("textID", "text");
        l.registerLocaleResource(TEST_LOCALE, table);

        OrderedMap<String,String> localeData = l.getLocaleData(TEST_LOCALE);

        boolean result = l.addAvailableLocale(TEST_LOCALE);
        if (result) {
            fail("Localizer overwrote existing locale");
        }

        OrderedMap<String,String> newLocaleData = l.getLocaleData(TEST_LOCALE);
        if (!localeData.equals(newLocaleData)) {
            fail("Localizer overwrote existing locale");
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
        } catch (UnregisteredLocaleException nsee) {
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
        } catch (UnregisteredLocaleException nsee) {
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
        } catch (UnregisteredLocaleException nsee) {
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
        } catch (UnregisteredLocaleException nsee) {
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

        if (!l.getLocaleMap(TEST_LOCALE).equals(l.getLocaleData(TEST_LOCALE))) {
            fail();
        }
    }

    public void testGetLocaleMapNotExist () {
        Localizer l = new Localizer();
        final String TEST_LOCALE = "test";

        try {
            l.getLocaleMap(TEST_LOCALE);

            fail("Did not throw exception when getting locale mapping for non-existent locale");
        } catch (UnregisteredLocaleException nsee) {
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
        TableLocaleSource table = new TableLocaleSource();
        table.setLocaleMapping("textID", "text");
        l.registerLocaleResource(TEST_LOCALE, table);

        if (!l.hasMapping(TEST_LOCALE, "textID")) {
            fail("Localizer does not contain newly added text mapping");
        }
        if (!"text".equals(l.getLocaleData(TEST_LOCALE).get("textID"))) {
            fail("Newly added text mapping does not match source");
        }
    }


    public void testTextMappingOverwrite () {
        Localizer l = new Localizer();
        final String TEST_LOCALE = "test";

        l.addAvailableLocale(TEST_LOCALE);
        TableLocaleSource table = new TableLocaleSource();

        table.setLocaleMapping("textID", "oldText");

        table.setLocaleMapping("textID", "newText");

        l.registerLocaleResource(TEST_LOCALE, table);

        if (!l.hasMapping(TEST_LOCALE, "textID")) {
            fail("Localizer does not contain overwritten text mapping");
        }
        if (!"newText".equals(l.getLocaleData(TEST_LOCALE).get("textID"))) {
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

    //private static final int BASE_FORM = 1;
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
        } catch (NoLocalizedTextException nsee) {
            if (expected != null) {
                fail("Got unexpected exception");
            }
        }
    }

    private Localizer buildLocalizer (int i, int j, int k, String ourLocale, String otherLocale) {
        Localizer l = new Localizer(i / 2 == 0, i % 2 == 0);

        TableLocaleSource firstLocale = new TableLocaleSource();
        TableLocaleSource secondLocale = new TableLocaleSource();

        if (j / 2 == 0 || "default".equals(ourLocale))
            firstLocale.setLocaleMapping("textID", "text:" + ourLocale + ":base");
        if (j % 2 == 0 || "default".equals(ourLocale))
            firstLocale.setLocaleMapping("textID;form", "text:" + ourLocale + ":form");

        if (otherLocale != null) {
            if (k / 2 == 0 || "default".equals(otherLocale))
                secondLocale.setLocaleMapping("textID", "text:" + otherLocale + ":base");
            if (k % 2 == 0 || "default".equals(otherLocale))
                secondLocale.setLocaleMapping("textID;form", "text:" + otherLocale + ":form");
        }

        l.addAvailableLocale(ourLocale);
        l.registerLocaleResource(ourLocale, firstLocale);

        if (otherLocale != null) {
            l.addAvailableLocale(otherLocale);
            l.registerLocaleResource(otherLocale, secondLocale);
        }
        if (l.hasLocale("default")) {
            l.setDefaultLocale("default");
        }

        l.setLocale(ourLocale);

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
        TableLocaleSource table = new TableLocaleSource();
        l.addAvailableLocale("test");
        l.setDefaultLocale("test");

        table.setLocaleMapping("textID", "text");
        l.registerLocaleResource("test", table);

        try {
            l.getText("textID");

            fail("Retrieved current locale text when current locale not set");
        } catch (UnregisteredLocaleException nsee) {
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

        TableLocaleSource table = new TableLocaleSource();

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
            l.registerLocaleResource(null, new TableLocaleSource());

            fail("setLocaleData: Did not get expected null pointer exception");
        } catch (NullPointerException npe) {
            //expected
        }

        try {
            l.registerLocaleResource("test", null);

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
        } catch (UnregisteredLocaleException nsee) {
            //expected
        }

        try {
            table.setLocaleMapping(null, "text");

            fail("setLocaleMapping: Did not get expected null pointer exception");
        } catch (NullPointerException npe) {
            //expected
        }

        try {
            table.setLocaleMapping(null, null);

            fail("setLocaleMapping: Did not get expected null pointer exception");
        } catch (NullPointerException npe) {
            //expected
        }

        try {
            l.hasMapping(null, "textID");

            fail("hasMapping: Did not get expected exception");
        } catch (UnregisteredLocaleException nsee) {
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
            l.getText("textID", (String)null);

            fail("getText: Did not get expected exception");
        } catch (UnregisteredLocaleException nsee) {
            //expected
        }

        try {
            l.getText(null, "test");

            fail("getText: Did not get expected null pointer exception");
        } catch (NullPointerException npe) {
            //expected
        }
    }

    public void testSerialization () {
        Localizer l = new Localizer(true, true);
        TableLocaleSource firstLocale = new TableLocaleSource();
        TableLocaleSource secondLocale = new TableLocaleSource();
        TableLocaleSource finalLocale = new TableLocaleSource();

        testSerialize(l, "empty 1");
        testSerialize(new Localizer(false, false), "empty 2");
        testSerialize(new Localizer(true, false), "empty 3");
        testSerialize(new Localizer(false, true), "empty 4");

        l.addAvailableLocale("locale1");
        testSerialize(l, "one empty locale");

        l.addAvailableLocale("locale2");
        testSerialize(l, "two empty locales");

        l.setDefaultLocale("locale2");
        testSerialize(l, "two empty locales + default");

        l.setToDefault();
        testSerialize(l, "two empty locales + default/current");

        l.setLocale("locale1");
        testSerialize(l, "two empty locales + default/current 2");

        l.setDefaultLocale(null);
        testSerialize(l, "two empty locales + current");

        l.registerLocaleResource("locale1", firstLocale);
        l.registerLocaleResource("locale2", secondLocale);
        firstLocale.setLocaleMapping("id1", "text1");
        testSerialize(l, "locales with data 1");
        firstLocale.setLocaleMapping("id2", "text2");
        testSerialize(l, "locales with data 2");

        secondLocale.setLocaleMapping("id1", "text1");
        secondLocale.setLocaleMapping("id2", "text2");
        secondLocale.setLocaleMapping("id3", "text3");
        testSerialize(l, "locales with data 3");

        secondLocale.setLocaleMapping("id2", null);
        testSerialize(l, "locales with data 4");

        finalLocale.setLocaleMapping("id1", "text1");
        finalLocale.setLocaleMapping("id4", "text4");
        l.registerLocaleResource("locale3", finalLocale);
        testSerialize(l, "locales with data 5");

        l.destroyLocale("locale2");
        testSerialize(l, "locales with data 6");
    }

    public void testLinearSub() {
        final String F = "first";
        final String S = "second";

        final String C = "${0}";

        final String D = "${1}${0}";

        final String[] res = new String[] {"One", "Two"};


        assertEquals(Localizer.processArguments("${0}", new String[] {F}), F);
        assertEquals(Localizer.processArguments("${0},${1}", new String[] {F,S}), F + "," + S);
        assertEquals(Localizer.processArguments("testing ${0}", new String[] {F}), "testing " + F);

        assertEquals(Localizer.processArguments("1${arbitrary}2", new String[] {F}), "1" + F + "2");

        final String[] holder = new String[1];

        runAsync(new Runnable() { public void run() {
                holder[0] = Localizer.processArguments("${0}", new String[] {C});
            }});

        assertEquals(holder[0], C);


        runAsync(new Runnable() { public void run() {
            holder[0] = Localizer.processArguments("${0}", new String[] {D});
        }});

        assertEquals(holder[0], D);

        runAsync(new Runnable() { public void run() {
            holder[0] = Localizer.processArguments(holder[0], res);
        }});

        assertEquals(holder[0], res[1] + res[0]);

        runAsync(new Runnable() { public void run() {
            holder[0] = Localizer.processArguments("$ {0} ${1}", res);
        }});

        assertEquals(holder[0], "$ {0} " + res[1]);

    }

    private void runAsync(Runnable test) {
        Thread t = new Thread(test);
        t.start();
        try {
            t.join(50);
        } catch (InterruptedException e) {

        }
        if(t.isAlive()) {
            throw new RuntimeException("Failed to return from recursive argument processing");
        }
    }

    public void testHashSub() {
        final String F = "first";
        final String S = "second";
        HashMap<String,String> h = new HashMap<String,String>();
        h.put("fir", F);
        h.put("also first", F);
        h.put("sec", S);

        assertEquals(Localizer.processArguments("${fir}",h), F);
        assertEquals(Localizer.processArguments("${fir},${sec}",h), F+","+S);
        assertEquals(Localizer.processArguments("${sec},${fir}",h), S+","+F);
        assertEquals(Localizer.processArguments("${empty}",h), "${empty}");
        assertEquals(Localizer.processArguments("${fir},${fir},${also first}",h), F+","+F+","+F);
    }


    public void testFallbacks() {
        Localizer localizer =  new Localizer(true,true);

        localizer.addAvailableLocale("one");
        localizer.addAvailableLocale("two");

        TableLocaleSource firstLocale = new TableLocaleSource();
        firstLocale.setLocaleMapping("data", "val");
        firstLocale.setLocaleMapping("data2", "vald2");
        localizer.registerLocaleResource("one", firstLocale);

        TableLocaleSource secondLocale = new TableLocaleSource();
        firstLocale.setLocaleMapping("data", "val2");
        localizer.registerLocaleResource("two", secondLocale);
        localizer.setDefaultLocale("one");

        localizer.setLocale("two");

        String text = localizer.getText("data2");
        assertEquals("fallback", text, "vald2");
        String shouldBeNull = localizer.getText("noexist");
        assertNull("Localizer didn't return null value", shouldBeNull);

        localizer.setToDefault();

        shouldBeNull = localizer.getText("noexist");
        assertNull("Localizer didn't return null value", shouldBeNull);
        assertNull("Localizer didn't return null value", shouldBeNull);
    }
}
