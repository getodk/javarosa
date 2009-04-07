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
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;

public class SelectMultiDataTests extends TestCase {
	QuestionDef question;
	
	Selection one;
	Selection two;
	Selection three;
	
	Vector firstTwo;
	Vector lastTwo;	
	Vector invalid;
	
	private static int NUM_TESTS = 5;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		question = new QuestionDef();
		question.setID(57);
		
		OrderedHashtable oh = new OrderedHashtable();
		Vector v = new Vector();
		for (int i = 0; i < 4; i++) {
			oh.put("Selection " + i, "Selection " + i);
			v.addElement(new Boolean(false));
		}	
		question.setSelectItemIDs(oh, v, null);
		question.localizeSelectMap(null);
				
		one = new Selection(1, question);
		two = new Selection(2, question);
		three = new Selection(3, question);
		
		firstTwo = new Vector();
		firstTwo.addElement(one);
		firstTwo.addElement(two);
		
		lastTwo = new Vector();
		lastTwo.addElement(two);
		lastTwo.addElement(three);
		
		invalid = new Vector();
		invalid.addElement(three);
		invalid.addElement(new Integer(12));
		invalid.addElement(one);
	}
	
	public SelectMultiDataTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public SelectMultiDataTests(String name) {
		super(name);
	}

	public SelectMultiDataTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new SelectMultiDataTests("SelectMultiData Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((SelectMultiDataTests)tc).testMaster(testID);
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
		case 4: testBadDataTypes(); break;
		case 5: testVectorImmutability(); break;
		}
	}
	
	public void testGetData() {
		SelectOneData data = new SelectOneData(one);
		assertEquals("SelectOneData's getValue returned an incorrect SelectOne", data.getValue(), one);
		
	}
	public void testSetData() {
		SelectMultiData data = new SelectMultiData(firstTwo);
		data.setValue(lastTwo);
		
		assertTrue("SelectMultiData did not set value properly. Maintained old value.", !(data.getValue().equals(firstTwo)));
		assertEquals("SelectMultiData did not properly set value ", data.getValue(), lastTwo);
		
		data.setValue(firstTwo);
		assertTrue("SelectMultiData did not set value properly. Maintained old value.", !(data.getValue().equals(lastTwo)));
		assertEquals("SelectMultiData did not properly reset value ", data.getValue(), firstTwo);
		
	}
	public void testNullData() {
		boolean exceptionThrown = false;
		SelectMultiData data = new SelectMultiData();
		data.setValue(firstTwo);
		try { 
			data.setValue(null);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("SelectMultiData failed to throw an exception when setting null data", exceptionThrown);
		assertTrue("SelectMultiData overwrote existing value on incorrect input", data.getValue().equals(firstTwo));
	}
	
	public void testVectorImmutability() {
		SelectMultiData data = new SelectMultiData(firstTwo);
		Selection[] copy = new Selection[firstTwo.size()];
		firstTwo.copyInto(copy);
		firstTwo.setElementAt(two, 0);
		firstTwo.removeElementAt(1);
		
		Vector internal = (Vector)data.getValue();

		assertVectorIdentity("External Reference: ", internal, copy);
		
		data.setValue(lastTwo);
		Vector start = (Vector)data.getValue();
		
		Selection[] external = new Selection[start.size()];
		start.copyInto(external);
		
		start.removeElementAt(1);
		start.setElementAt(one, 0);
		
		assertVectorIdentity("Internal Reference: ", (Vector)data.getValue(), external);
	}
	
	private void assertVectorIdentity(String messageHeader, Vector v, Selection[] a) {
		
		assertEquals(messageHeader + "SelectMultiData's internal representation was violated. Vector size changed.",v.size(),a.length);
		
		for(int i = 0 ; i < v.size(); ++i) {
			Selection internalValue = (Selection)v.elementAt(i);
			Selection copyValue = a[i];
			
			assertEquals(messageHeader + "SelectMultiData's internal representation was violated. Element " + i + "changed.",internalValue,copyValue);
		}
	}
	
	public void testBadDataTypes() {
		boolean failure = false;
		SelectMultiData data = new SelectMultiData(firstTwo);
		try {
			data.setValue(invalid);
			data = new SelectMultiData(invalid);
		} catch(Exception e) {
			failure = true;
		}
		assertTrue("SelectMultiData did not throw a proper exception while being set to invalid data.",failure);
		
		Selection[] values = new Selection[firstTwo.size()];
		firstTwo.copyInto(values);
		assertVectorIdentity("Ensure not overwritten: ", (Vector)data.getValue(), values);
	}
}
