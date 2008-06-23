package org.javarosa.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.kxml2.io.KXmlParser;

public class JavaRosaBasicTests extends TestCase {
	
	public JavaRosaBasicTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public JavaRosaBasicTests(String name) {
		super(name);
	}
	
	public JavaRosaBasicTests() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest((new JavaRosaBasicTests("testSingletons", new TestMethod() {
			public void run(TestCase tc) { ((JavaRosaBasicTests)tc).testXMLParsing(); }
		})));
				
		return aSuite;
	}
	
	public void testXMLParsing() {
		this.assertTrue("XML Parser Doesn't Construct", new KXmlParser() != null);
	}
}
