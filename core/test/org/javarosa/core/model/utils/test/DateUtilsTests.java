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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.DateUtils.DateFields;

public class DateUtilsTests extends TestCase {
	
	private static int NUM_TESTS = 5;
	
	Date currentTime;
	Date minusOneHour;
	
	
	public DateUtilsTests(String name, TestMethod rTestMethod){
		super(name, rTestMethod);
	}
	
	public DateUtilsTests(String name){
		super(name);
	}
	
	public DateUtilsTests(){
		super();
	}

	protected void setUp() throws Exception{
		super.setUp();
		
		currentTime = new Date();
		minusOneHour = new Date(new Date().getTime() - (1000*60));
		
	}
	
	public Test suite(){
		
		TestSuite dateSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			dateSuite.addTest(new DateUtilsTests("DateUtilData Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((DateUtilsTests)tc).testMaster(testID);
				}
			}));
		}

		return dateSuite;
		
	}

	public void testMaster(int testID) {
		
	
		switch (testID) {
		case 1: testGetXMLStringValueFormat(); break;
		case 2: testSetDates(); break;
		case 3: testNullDates(); break;
		case 4: testTimeParses(); break;
		case 5: testParity(); break;
		}
	}
	
	/**
	 * This test ensures that the Strings returned
	 * by the getXMLStringValue function are in
	 * the proper XML compliant format.
	 */
	private void testGetXMLStringValueFormat() {
		String currentDate = DateUtils.getXMLStringValue(currentTime);
		assertEquals("The date string was not of the proper length", currentDate.length(),"YYYY-MM-DD".length());
		assertEquals("The date string does not have proper year formatting", currentDate.indexOf("-"),"YYYY-".indexOf("-"));
		try {
			Integer.parseInt(currentDate.substring(0, 4));
		} catch (NumberFormatException e) {
			fail("The Year value was not a valid integer");
		}
		try {
			Integer.parseInt(currentDate.substring(5, 7));
		} catch (NumberFormatException e) {
			fail("The Month value was not a valid integer");
		}
		try {
			Integer.parseInt(currentDate.substring(8, 10));
		} catch (NumberFormatException e) {
			fail("The Day value was not a valid integer");
		}
	}

	private void testNullDates() {
		// TODO Auto-generated method stub
		
	}

	private void testSetDates() {
		// TODO Auto-generated method stub
		
	}
	
	private void testTimeParses() {
		//This is all kind of tricky. We need to assume J2ME level compliance, so
		//dates won't every be assumed to have an intrinsic timezone, they'll be
		//assumed to be in the phone's default timezone
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		testTime("10:00", 1000*60*60*10);
		testTime("10:00Z", 1000*60*60*10);
		
		testTime("10:00+02", 1000*60*60*8);
		testTime("10:00-02", 1000*60*60*12);
		
		testTime("10:00+02:30", 1000*60*6*75);
		testTime("10:00-02:30", 1000*60*6*125);
		
		TimeZone offsetTwoHours = TimeZone.getTimeZone("GMT+02");
		
		TimeZone.setDefault(offsetTwoHours);
		
		testTime("10:00", 1000*60*60*10);
		testTime("10:00Z", 1000*60*60*12);
		
		testTime("10:00+02", 1000*60*60*10);
		testTime("10:00-02", 1000*60*60*14);
		
		testTime("10:00+02:30", 1000*60*6*95);
		testTime("10:00-02:30", 1000*60*6*145);

		TimeZone offsetMinusTwoHours = TimeZone.getTimeZone("GMT-02");
		
		TimeZone.setDefault(offsetMinusTwoHours);
		
		testTime("14:00", 1000*60*60*14);
		testTime("14:00Z", 1000*60*60*12);
		
		testTime("14:00+02", 1000*60*60*10);
		testTime("14:00-02", 1000*60*60*14);
		
		testTime("14:00+02:30", 1000*60*6*95);
		testTime("14:00-02:30", 1000*60*6*145);


		TimeZone offsetPlusHalf = TimeZone.getTimeZone("GMT+0230");
		
		TimeZone.setDefault(offsetPlusHalf);
		
		testTime("14:00", 1000*60*6*140);
		testTime("14:00Z", 1000*60*6*165);
		
		testTime("14:00+02", 1000*60*6*145);
		testTime("14:00-02", 1000*60*6*185);
		
		testTime("14:00+02:30", 1000*60*6*140);
		testTime("14:00-02:30", 1000*60*6*190);
		
		testTime("14:00+04:00", 1000*60*6*125);
		
		TimeZone.setDefault(null);
	}
	
	private void testTime(String in, long test) {
		try{ 
			Date d = DateUtils.parseTime(in);
			
			//getTime here should always assume that it's in the UTC context, since that's the
			//only available mode for j2me 1.3 (IE: Dates will always come out flat). We'll 
			//simulate that here by offsetting.
			long offset = getOffset();
			
			long value = d.getTime() + offset;
			
			assertEquals("Fail: " + in + "(" + TimeZone.getDefault().getDisplayName() + ")", test, value);
		} catch(Exception e) {
			e.printStackTrace();
			fail("Error: " + in + e.getMessage());
		}
	}
	
	private long getOffset() {
		DateFields df = new DateFields();
		Date d = DateUtils.getDate(df);

		return -d.getTime();
	}
	
	private void testParity() {
		testCycle(new Date(1300139579000l));
		testCycle(new Date(0));
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		testCycle(new Date(1300139579000l));
		testCycle(new Date(0));
		
		TimeZone offsetTwoHours = TimeZone.getTimeZone("GMT+02");
		
		TimeZone.setDefault(offsetTwoHours);
		
		testCycle(new Date(1300139579000l));
		testCycle(new Date(0));

		
		TimeZone offTwoHalf = TimeZone.getTimeZone("GMT+0230");
		
		TimeZone.setDefault(offTwoHalf);
		
		testCycle(new Date(1300139579000l));
		testCycle(new Date(0));
		
		TimeZone offMinTwoHalf = TimeZone.getTimeZone("GMT-0230");
		
		TimeZone.setDefault(offMinTwoHalf);
		
		testCycle(new Date(1300139579000l));
		testCycle(new Date(0));


		
	}
	
	private void testCycle(Date in) {
		try{
			String formatted =DateUtils.formatDateTime(in, DateUtils.FORMAT_ISO8601);
			Date out = DateUtils.parseDateTime(formatted);
			assertEquals("Fail:", in.getTime(), out.getTime());

		} catch(Exception e) {
			e.printStackTrace();
			fail("Error: " + in + e.getMessage());
		}

	}
/*
	private void testGetData() {
		
		Date rep = (Date)DateUtils.getDate(2008, 9, 20);
		rep.setTime(rep.getTime() - 1000);
		
		
		//Testing the formatDateToTimeStamp
		assertEquals("DateUtil's formatDateToTimeStamp returned an incorret Time", DateUtils.formatDateToTimeStamp(currentTime), DateUtils.getXMLStringValue(currentTime));
		
		
		Date temp = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		assertEquals("DateUtil's formatDateToTimeStamp returned an incorret Time", DateUtils.formatDateToTimeStamp(temp),DateUtils.getShortStringValue(temp));
		
		//formatDateToTimeStamp
		
		//Testing the getShortStringValue
		assertEquals("DateUtils's getShortStringValue returned an incorrect Time", DateUtils.getShortStringValue(currentTime), currentTime.toString());
		
		Date temp2 = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		
		assertEquals("DateUtils's getShortStringValue returned an incorrect Time", DateUtils.getShortStringValue(temp2), temp2.toString());
		
		//getShortStringValue
		
		//Testing the getXMLStringValue
		assertEquals("DateUtils's getXMLStringValue returned an incorrect Time", DateUtils.getXMLStringValue(currentTime), currentTime.toString());
		
		Date temp3 = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		
		assertEquals("DateUtils's getXMLStringValue returned an incorrect Time", DateUtils.getXMLStringValue(temp3), temp3.toString());
		
		//getXMLStringValue
		
		
		//Testing the getDateFromString
		
		String date = currentTime.toString();
		assertEquals("DateUtils's getDateFromString returned an incorrect Time", DateUtils.getDateFromString(date), currentTime);
		
		Date temp4 = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		
		assertEquals("DateUtils's getDateFromString returned an incorrect Time", DateUtils.getDateFromString(temp4.toString()), temp4);
		
		//getDateFromString
		
		
		//Testing the get24HourTimeFromDate
		assertEquals("DateUtils's get24HourTimeFromDate returned an incorrect Time", DateUtils.get24HourTimeFromDate(currentTime), currentTime);
		
		Date temp1 = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		assertEquals("DateUtils's get24HourTimeFromDate was mutated incorrectly", DateUtils.get24HourTimeFromDate(temp1), temp1);
		
		//get24HourTimeFromDate		
		
		//Testing method getDate
		
		int hour = 12;
		int minute = 50;
		int second = 60;
		
		currentTime.setTime(125060);

		assertEquals("DateUtils's getDate returned an incorrect Time", DateUtils.getDate(hour, minute, second), currentTime);
		
		Date now = new Date();
		currentTime.setTime(now.getTime());
		
		//getDate
		
		//Testing the Method roundDate
		
		Date testDate = new Date();
		testDate.setTime(000000);
		assertEquals("DateUtils's roundDate returned an incorrect Time", DateUtils.roundDate(testDate), currentTime);
		
		Date temp5 = new Date(currentTime.getTime());
		Date testDate2 = new Date();
		testDate2.setTime(000000);
		
		//currentTime.setTime(1234);
		assertEquals("DateUtils's roundDate was mutated incorrectly", DateUtils.roundDate(temp5), testDate2);
		
		//roundDate		
		
		//Testing the Method get24HourTimeFromDate
		
		assertEquals("DateUtils's get24HourTimeFromDate returned an incorrect Time", DateUtils.get24HourTimeFromDate(currentTime), currentTime);
		
		Date temp6 = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		assertEquals("DateUtils's get24HourTimeFromDate was mutated incorrectly", DateUtils.get24HourTimeFromDate(temp6), temp6);
		
		//get24HourTimeFromDate
		
		//Testing the Method daysInMonth
		// int month = 9;
		// int year = 2008;
		 
		//assertSame("DateUtils's daysInMonth returned an incorrect Time", DateUtils.daysInMonth(month, year));
		
		//Date temp7 = new Date(currentTime.getTime());
		//currentTime.setTime(1234);
		//assertEquals("DateUtils's daysInMonth was mutated incorrectly", DateUtils.daysInMonth(month, year), temp6);
		
		//daysInMonth
		
	}
	*/
}
