package org.javarosa.core.util.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Vector;

import org.javarosa.core.util.PrefixTree;

public class PrefixTreeTest extends TestCase  {
	public PrefixTreeTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public PrefixTreeTest(String name) {
		super(name);
	}

	public PrefixTreeTest() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest((new PrefixTreeTest("Prefix Tree Test Suite", new TestMethod() {
			public void run(TestCase tc) {
				((PrefixTreeTest)tc).doTests();
			}
		})));
		return aSuite;
	}

	public void add (PrefixTree t, String s) {
		t.addString(s);
		System.out.println(t.toString());
		
		Vector v = t.getStrings();
		for (int i = 0; i < v.size(); i++) {
			System.out.println((String)v.elementAt(i));
		}
	}
	
	public void doTests() {
		
		PrefixTree t = new PrefixTree();	
		System.out.println(t.toString());
		
		add(t, "abcde");
		add(t, "abcdefghij");
		add(t, "abcdefghijklmno");
		add(t, "abcde");
		add(t, "abcdefg");
		add(t, "xyz");
		add(t, "abcdexyz");
		add(t, "abcppppp");
		
	}
}
