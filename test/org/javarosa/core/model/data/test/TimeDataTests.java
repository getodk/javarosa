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

import java.util.Date;

import org.javarosa.core.model.data.TimeData;

public class TimeDataTests extends TestCase {
	Date now;
	Date minusOneHour;
	
	private static int NUM_TESTS = 3;
	
	public void setUp() throws Exception {
		super.setUp();
		
		now = new Date();
		minusOneHour = new Date(new Date().getTime() - (1000*60));
	}

	public TimeDataTests(String name) {
		super(name);
		System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
	}

	public static Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new TimeDataTests(testMaster(testID)));
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
		TimeData data = new TimeData(now);
		assertEquals("TimeData's getValue returned an incorrect Time", data.getValue(), now);
		Date temp = new Date(now.getTime());
		now.setTime(1234);
		assertEquals("TimeData's getValue was mutated incorrectly", data.getValue(), temp);
		
		Date rep = (Date)data.getValue();
		rep.setTime(rep.getTime() - 1000);
		
		assertEquals("TimeData's getValue was mutated incorrectly", data.getValue(), temp);
		
	}
	public void testSetData() {
		TimeData data = new TimeData(now);
		data.setValue(minusOneHour);
		
		assertTrue("TimeData did not set value properly. Maintained old value.", !(data.getValue().equals(now)));
		assertEquals("TimeData did not properly set value ", data.getValue(), minusOneHour);
		
		data.setValue(now);
		assertTrue("TimeData did not set value properly. Maintained old value.", !(data.getValue().equals(minusOneHour)));
		assertEquals("TimeData did not properly reset value ", data.getValue(), now);
		
		Date temp = new Date(now.getTime());
		now.setTime(now.getTime() - 1324);
		
		assertEquals("TimeData's value was mutated incorrectly", data.getValue(), temp);
	}
	public void testNullData() {
		boolean exceptionThrown = false;
		TimeData data = new TimeData();
		data.setValue(now);
		try { 
			data.setValue(null);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("TimeData failed to throw an exception when setting null data", exceptionThrown);
		assertTrue("TimeData overwrote existing value on incorrect input", data.getValue().equals(now));
	}
}
