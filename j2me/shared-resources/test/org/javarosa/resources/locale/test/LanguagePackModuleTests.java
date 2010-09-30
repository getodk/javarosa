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

package org.javarosa.resources.locale.test;


import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.resources.locale.LanguagePackModule;

public class LanguagePackModuleTests extends TestCase  {
	public final int NUM_TESTS = 1 ;

	public LanguagePackModuleTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public LanguagePackModuleTests(String name) {
		super(name);
	}

	public LanguagePackModuleTests() {
		super();
	}

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new LanguagePackModuleTests("Locale File Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((LanguagePackModuleTests)tc).testMaster(testID);
				}
			}));
		}

		return aSuite;
	}

	public void testMaster (int testID) {
		//System.out.println("running " + testID);

		switch (testID) {
		case 1: testValidLanguageFiles(); break;
		}
	}

	public void testValidLanguageFiles () {
		new LanguagePackModule().registerModule();
		for(String locale : LanguagePackModule.locales) {
			try {
				Localization.setLocale(locale);
			} catch(Exception e) {
				fail("Malformed Locale File in the Core Language Pack! Error: " + e.getMessage());
			}
		}
	}
}
