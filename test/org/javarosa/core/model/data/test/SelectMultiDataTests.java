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
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;

import java.util.ArrayList;
import java.util.List;

public class SelectMultiDataTests extends TestCase {
	QuestionDef question;
	
	Selection one;
	Selection two;
	Selection three;

   List<Selection> firstTwo;
   List<Selection> lastTwo;
   List invalid;
	
	private static int NUM_TESTS = 5;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		
		question = new QuestionDef();
		
		for (int i = 0; i < 4; i++) {
			question.addSelectChoice(new SelectChoice("","Selection" + i, "Selection " + i, false));
		}	
				
		one = new Selection("Selection 1");
		one.attachChoice(question);
		two = new Selection("Selection 2");
		two.attachChoice(question);
		three = new Selection("Selection 3");
		three.attachChoice(question);
		
		firstTwo = new ArrayList<Selection>();
		firstTwo.add(one);
		firstTwo.add(two);
		
		lastTwo = new ArrayList<Selection>();
		lastTwo.add(two);
		lastTwo.add(three);
		
		invalid = new ArrayList<Object>();
		invalid.add(three);
		invalid.add(new Integer(12));
		invalid.add(one);
	}

	public SelectMultiDataTests(String name) {
		super(name);
		System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
	}

	public static Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new SelectMultiDataTests(testMaster(testID)));
		}

		return aSuite;
	}

	public static String testMaster (int testID) {
		//System.out.println("running " + testID);
		
		switch (testID) {
		case 1: return "testGetData";
		case 2: return "testSetData";
		case 3: return "testNullData";
		case 4: return "testBadDataTypes";
		case 5: return "testVectorImmutability";
		}
		throw new IllegalStateException("Unexpected index");
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
		firstTwo.toArray(copy);
		firstTwo.set(0, two);
		firstTwo.remove(1);
		
		List<Selection> internal = (List<Selection>)data.getValue();

		assertVectorIdentity("External Reference: ", internal, copy);
		
		data.setValue(lastTwo);
        List<Selection> start = (List<Selection>)data.getValue();
		
		Selection[] external = new Selection[start.size()];
		start.toArray(external);
		
		start.remove(1);
		start.set(0, one);
		
		assertVectorIdentity("Internal Reference: ", (List<Selection>)data.getValue(), external);
	}
	
	private void assertVectorIdentity(String messageHeader, List<Selection> v, Selection[] a) {
		
		assertEquals(messageHeader + "SelectMultiData's internal representation was violated. Vector size changed.",v.size(),a.length);
		
		for(int i = 0 ; i < v.size(); ++i) {
			Selection internalValue = v.get(i);
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
		firstTwo.toArray(values);
		assertVectorIdentity("Ensure not overwritten: ", (List<Selection>)data.getValue(), values);
	}
}
