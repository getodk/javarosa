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

package org.javarosa.core.model.data.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;

public class SelectOneDataTests extends TestCase {
	QuestionDef question;
	
	Selection one;
	Selection two;
	
	private static int NUM_TESTS = 3;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		
		question = new QuestionDef();
		question.setID(57);
		
		for (int i = 0; i < 3; i++) {
			question.addSelectChoice(new SelectChoice("","Selection" + i, "Selection" + i, false));
		}	
		
		one = new Selection("Selection1");
		one.attachChoice(question);
		two = new Selection("Selection2");
		two.attachChoice(question);
	}

	public SelectOneDataTests(String name) {
		super(name);
		System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			suite.addTest(new SelectOneDataTests(testMaster(testID)));
		}

		return suite;
	}

	public static String testMaster (int testID) {
		//System.out.println("running " + testID);
		
		switch (testID) {
		case 1: return "testGetData";
		case 2: return "testSetData";
		case 3: return "testNullData";
		}
		throw new IllegalStateException("Unexpected index");
	}
	
	public void testGetData() {
		SelectOneData data = new SelectOneData(one);
		assertEquals("SelectOneData's getValue returned an incorrect SelectOne", data.getValue(), one);
		
	}

	public void testSetData() {
		SelectOneData data = new SelectOneData(one);
		data.setValue(two);
		
		assertTrue("SelectOneData did not set value properly. Maintained old value.", !(data.getValue().equals(one)));
		assertEquals("SelectOneData did not properly set value ", data.getValue(), two);
		
		data.setValue(one);
		assertTrue("SelectOneData did not set value properly. Maintained old value.", !(data.getValue().equals(two)));
		assertEquals("SelectOneData did not properly reset value ", data.getValue(), one);
		
	}

	public void testNullData() {
		boolean exceptionThrown = false;
		SelectOneData data = new SelectOneData();
		data.setValue(one);
		try { 
			data.setValue(null);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("SelectOneData failed to throw an exception when setting null data", exceptionThrown);
		assertTrue("SelectOneData overwrote existing value on incorrect input", data.getValue().equals(one));
	}
}
