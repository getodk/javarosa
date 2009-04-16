/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Vector;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;

public class SelectOneDataTests extends TestCase {
	QuestionDef question;
	
	Selection one;
	Selection two;
	
	private static int NUM_TESTS = 3;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		question = new QuestionDef();
		question.setID(57);
		
		OrderedHashtable oh = new OrderedHashtable();
		Vector v = new Vector();
		for (int i = 0; i < 3; i++) {
			oh.put("Selection" + i, "Selection" + i);
			v.addElement(new Boolean(false));
		}	
		question.setSelectItemIDs(oh, v, null);
		question.localizeSelectMap(null);
		
		one = new Selection("Selection1");
		one.setQuestionDef(question);
		two = new Selection("Selection2");
		two.setQuestionDef(question);
	}
	
	public SelectOneDataTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public SelectOneDataTests(String name) {
		super(name);
	}

	public SelectOneDataTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new SelectOneDataTests("SelectOneData Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((SelectOneDataTests)tc).testMaster(testID);
				}
			}));
		}

		return aSuite;
	}
	public void testMaster (int testID) {
		//System.out.println("running " + testID);
		
		switch (testID) {
		case 1: testGetData(); break;
		case 2: testSetData(); break;
		case 3: testNullData(); break;
		}
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
