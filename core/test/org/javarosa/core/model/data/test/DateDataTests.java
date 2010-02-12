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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Date;

import org.javarosa.core.model.data.DateData;

public class DateDataTests extends TestCase{
	
	Date today;
	Date notToday;
	
	private static int NUM_TESTS = 4;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		today = new Date();
		notToday = new Date(today.getTime() - today.getTime()/2);
	}
	
	public DateDataTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public DateDataTests(String name) {
		super(name);
	}

	public DateDataTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new DateDataTests("DateData Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((DateDataTests)tc).testMaster(testID);
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
		case 3: testDisplay(); break;
		case 4: testNullData(); break;
		}
	}
	
	public void testGetData() {
		DateData data = new DateData(today);
		assertEquals("DateData's getValue returned an incorrect date", data.getValue(), today);
		Date temp = new Date(today.getTime());
		today.setTime(1234);
		assertEquals("DateData's getValue was mutated incorrectly", data.getValue(), temp);
		
		Date rep = (Date)data.getValue();
		rep.setTime(rep.getTime() - 1000);
		
		assertEquals("DateData's getValue was mutated incorrectly", data.getValue(), temp);
	}
	public void testSetData() {
		DateData data = new DateData(notToday);
		data.setValue(today);
		
		assertTrue("DateData did not set value properly. Maintained old value.", !(data.getValue().equals(notToday)));
		assertEquals("DateData did not properly set value ", data.getValue(), today);
		
		data.setValue(notToday);
		assertTrue("DateData did not set value properly. Maintained old value.", !(data.getValue().equals(today)));
		assertEquals("DateData did not properly reset value ", data.getValue(), notToday);
		
		Date temp = new Date(notToday.getTime());
		notToday.setTime(notToday.getTime() - 1324);
		
		assertEquals("DateData's value was mutated incorrectly", data.getValue(), temp);
	}
	
	public void testDisplay() {
		// We don't actually want this, because the Date's getDisplayText code should be moved to a library
	}
	
	public void testNullData() {
		boolean exceptionThrown = false;
		DateData data = new DateData();
		data.setValue(today);
		try { 
			data.setValue(null);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("DateData failed to throw an exception when setting null data", exceptionThrown);
		assertTrue("DateData overwrote existing value on incorrect input", data.getValue().equals(today));
	}
}
