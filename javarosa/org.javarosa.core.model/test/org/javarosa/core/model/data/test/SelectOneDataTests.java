package org.javarosa.core.model.data.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
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
		
		OrderedHashtable oh = new OrderedHashtable();
		oh.put("Selection 1", "Selection 1");
		oh.put("Selection 2", "Selection 2");
		oh.put("Selection 3", "Selection 3");
		
		//question.setSelectItems();
		
		one = new Selection(1, question);
		two = new Selection(2, question);
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
