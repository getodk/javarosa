package org.javarosa.xpath.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.parser.XPathSyntaxException;


public class XPathTest extends TestCase{
	
	public XPathTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public XPathTest(String name) {
		super(name);
	}
	
	public XPathTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest((new XPathTest("Test Examples", new TestMethod() {
			public void run(TestCase tc) { ((XPathTest)tc).testSuccessSamples(); }
		})));
		aSuite.addTest((new XPathTest("Test Examples", new TestMethod() {
			public void run(TestCase tc) { ((XPathTest)tc).testFailSamples(); }
		})));
				
		return aSuite;
	}
	
	private void testXPathSuccess (String expr) {
		try{
			XPathParseTool.parseXPath(expr);
		} catch (XPathSyntaxException xse) {
			this.fail("XPath Parse test failed with Syntax Error for case: " + expr);
		}
	}
	
	private void testXPathFailure (String expr) {
		boolean exception = false;
		try{
			XPathParseTool.parseXPath(expr);
		} catch (XPathSyntaxException xse) {
			exception = true;
		}
		if(!exception) {
			this.fail("XPath Parse test should have failed with Syntax Error for case: " + expr);
		}
	}

	public void testSuccessSamples () {
		testXPathSuccess("/path/to/a/node");
		testXPathSuccess("/patient/sex = 'male' and /patient/age > 15");
		testXPathSuccess("../jr:hist-data/labs[@type=\"cd4\"]");
		testXPathSuccess("function_call(26*(7+3), //*, /im/child::an/descendant-or-self::x/path[3][true()])");
	}
	
	public void testFailSamples() {
		testXPathFailure("~ lexical error");
		testXPathFailure("syntax + ) error");
	}
}


