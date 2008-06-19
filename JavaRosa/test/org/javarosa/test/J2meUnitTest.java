package org.javarosa.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

public class J2meUnitTest extends TestCase {
	
	public J2meUnitTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public J2meUnitTest(String name) {
		super(name);
	}
	
	public J2meUnitTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest((new J2meUnitTest("testOne", new TestMethod() {
			public void run(TestCase tc) { ((J2meUnitTest)tc).testOne(); }
		})));
		aSuite.addTest((new J2meUnitTest("testTwo", new TestMethod() {
			public void run(TestCase tc) { ((J2meUnitTest)tc).testTwo(); }
		})));
		
		return aSuite;
	}
	
	public void testOne() {
		assertEquals(1, 1);
	}
	
	public void testTwo() {
		assertTrue(false);
	}
}
