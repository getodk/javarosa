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

package org.javarosa.core.io.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.javarosa.core.io.BufferedInputStream;
import org.javarosa.core.util.ArrayUtilities;

public class BufferedInputStreamTests extends TestCase{
	
	byte[] bytes;
	
	int[] sizesToTest = new int[] { 15, 64, 500, 1280, 2047, 2048, 2049, 5000, 10000, 23000};
	
	byte[][] arraysToTest;
	
	private static int NUM_TESTS = 2;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		Random r = new Random();
		
		arraysToTest = new byte[sizesToTest.length][];
		for(int i = 0 ; i < sizesToTest.length ; ++i) {
			arraysToTest[i] = new byte[sizesToTest[i]];
			r.nextBytes(arraysToTest[i]);
		}
	}
	
	public BufferedInputStreamTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public BufferedInputStreamTests(String name) {
		super(name);
	}

	public BufferedInputStreamTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new BufferedInputStreamTests("DateData Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((BufferedInputStreamTests)tc).testMaster(testID);
				}
			}));
		}

		return aSuite;
	}
	public void testMaster (int testID) {
		//System.out.println("running " + testID);
		
		switch (testID) {
		case 1: testBuffered(); break;
		case 2: testIndividual(); break;
		}
	}
	
	private void testBuffered() {
		//TODO: Test on this axis too?
		byte[] testBuffer = new byte[256];
		
		for(byte[] bytes : arraysToTest) {
			try {
				BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				boolean done = false; 
				while(!done ) {
					int read = bis.read(testBuffer);
					if(read == -1) {
						break;
					} else {
						baos.write(testBuffer, 0, read);
					}
				}
				
				
				if(!ArrayUtilities.arraysEqual(bytes, baos.toByteArray())) {
					this.fail("Bulk BufferedInputStream read failed at size " + bytes.length);
				}
			} catch(Exception e) {
				this.fail("Exception while testing bulk read for " + bytes.length + " size: " + e.getMessage());
				continue;
			}
		}
	}

	private void testIndividual() {
		//TODO: Almost identical to above
		for(byte[] bytes : arraysToTest) {
			try {
				BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
				int position = 0;
				
				boolean done = false; 
				while(!done ) {
					int read = bis.read();
					if(read == -1) {
						break;
					} else {
						if(bytes[position] != (byte)read) {
							this.fail("one-by-one BIS read failed at size " + bytes.length + " at position " + position);
						}
					}
					position++;
				}
				if(position != bytes.length) {
					this.fail("one-by-one BIS read failed to read full array of size " + bytes.length + " only read " + position);
				}
			} catch(Exception e) {
				this.fail("Exception while testing buffered read for " + bytes.length + " size: " + e.getMessage());
				continue;
			}
		}
	}
}
