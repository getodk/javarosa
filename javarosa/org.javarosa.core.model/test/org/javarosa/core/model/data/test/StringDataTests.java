package org.javarosa.core.model.data.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.model.data.StringData;

public class StringDataTests extends TestCase {
	String stringA;
	String stringB;
	
	private static int NUM_TESTS = 3;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		stringA = "string A";
		stringB = "string B";
	}
	
	public StringDataTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public StringDataTests(String name) {
		super(name);
	}

	public StringDataTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new StringDataTests("StringData Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((StringDataTests)tc).testMaster(testID);
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
		StringData data = new StringData(stringA);
		assertEquals("StringData's getValue returned an incorrect String", data.getValue(), stringA);
		
	}
	public void testSetData() {
		StringData data = new StringData(stringA);
		data.setValue(stringB);
		
		assertTrue("StringData did not set value properly. Maintained old value.", !(data.getValue().equals(stringA)));
		assertEquals("StringData did not properly set value ", data.getValue(), stringB);
		
		data.setValue(stringA);
		assertTrue("StringData did not set value properly. Maintained old value.", !(data.getValue().equals(stringB)));
		assertEquals("StringData did not properly reset value ", data.getValue(), stringA);
	}
	public void testNullData() {
		boolean exceptionThrown = false;
		StringData data = new StringData();
		data.setValue(stringA);
		try { 
			data.setValue(null);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("StringData failed to throw an exception when setting null data", exceptionThrown);
		assertTrue("StringData overwrote existing value on incorrect input", data.getValue().equals(stringA));
	}
}
