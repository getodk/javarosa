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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

import org.javarosa.core.util.PrefixTree;

public class PrefixTreeTest extends TestCase  {

	public PrefixTreeTest(String name) {
		super(name);
		System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
	}

	public static Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest(new PrefixTreeTest("doTests"));
		return aSuite;
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

	public void add (PrefixTree t, String s) {
		t.addString(s);
		System.out.println(t.toString());
		
		List<String> v = t.getStrings();
		for (int i = 0; i < v.size(); i++) {
			System.out.println((String)v.get(i));
		}
	}
}
