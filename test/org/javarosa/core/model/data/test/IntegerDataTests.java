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

import org.javarosa.core.model.data.IntegerData;

public class IntegerDataTests extends TestCase {
	Integer one;
	Integer two;
	
	private static int NUM_TESTS = 3;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		
		one = new Integer(1);
		two = new Integer(2);
	}
	
	public IntegerDataTests(String name) {
		super(name);
		System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
	}

	public static Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new IntegerDataTests(testMaster(testID)));
		}

		return aSuite;
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
		IntegerData data = new IntegerData(one);
		assertEquals("IntegerData's getValue returned an incorrect integer", data.getValue(), one);
	}
	public void testSetData() {
		IntegerData data = new IntegerData(one);
		data.setValue(two);
		
		assertTrue("IntegerData did not set value properly. Maintained old value.", !(data.getValue().equals(one)));
		assertEquals("IntegerData did not properly set value ", data.getValue(), two);
		
		data.setValue(one);
		assertTrue("IntegerData did not set value properly. Maintained old value.", !(data.getValue().equals(two)));
		assertEquals("IntegerData did not properly reset value ", data.getValue(), one);
		
	}
	public void testNullData() {
		boolean exceptionThrown = false;
		IntegerData data = new IntegerData();
		data.setValue(one);
		try { 
			data.setValue(null);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("IntegerData failed to throw an exception when setting null data", exceptionThrown);
		assertTrue("IntegerData overwrote existing value on incorrect input", data.getValue().equals(one));
	}
}
