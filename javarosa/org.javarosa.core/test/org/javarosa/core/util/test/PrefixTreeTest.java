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
