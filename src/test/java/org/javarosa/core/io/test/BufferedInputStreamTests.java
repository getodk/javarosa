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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.javarosa.core.io.BufferedInputStream;
import org.javarosa.core.util.ArrayUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class BufferedInputStreamTests extends TestCase{
    private static final Logger logger = LoggerFactory.getLogger(BufferedInputStreamTests.class);

    byte[] bytes;

    int[] sizesToTest = new int[] { 15, 64, 500, 1280, 2047, 2048, 2049, 5000, 10000, 23000};

    byte[][] arraysToTest;

    private static int NUM_TESTS = 2;

    public void setUp() throws Exception {
        super.setUp();

        Random r = new Random();

        arraysToTest = new byte[sizesToTest.length][];
        for(int i = 0 ; i < sizesToTest.length ; ++i) {
            arraysToTest[i] = new byte[sizesToTest[i]];
            r.nextBytes(arraysToTest[i]);
        }
    }

    public BufferedInputStreamTests(String name) {
        super(name);
        logger.info("Running {} test: {}...", this.getClass().getName(), name);
    }

    public static Test suite() {
        TestSuite aSuite = new TestSuite();

        for (int i = 1; i <= NUM_TESTS; i++) {
            final int testID = i;

            aSuite.addTest(new BufferedInputStreamTests(testMaster(testID)));
        }

        return aSuite;
    }
    public static String testMaster (int testID) {
        switch (testID) {
        case 1: return "testBuffered";
        case 2: return "testIndividual";
        }
        throw new IllegalStateException("unexpected index");
    }

    public void testBuffered() {
        //TODO: Test on this axis too?
        byte[] testBuffer = new byte[256];

        for(byte[] bytes : arraysToTest) {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
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
                    fail("Bulk BufferedInputStream read failed at size " + bytes.length);
                }
            } catch(Exception e) {
                fail("Exception while testing bulk read for " + bytes.length + " size: " + e.getMessage());
                continue;
            } finally {
                if ( bis != null ) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        // ignore
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void testIndividual() {
        //TODO: Almost identical to above
        for(byte[] bytes : arraysToTest) {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
                int position = 0;

                boolean done = false;
                while(!done ) {
                    int read = bis.read();
                    if(read == -1) {
                        break;
                    } else {
                        if(bytes[position] != (byte)read) {
                            fail("one-by-one BIS read failed at size " + bytes.length + " at position " + position);
                        }
                    }
                    position++;
                }
                if(position != bytes.length) {
                    fail("one-by-one BIS read failed to read full array of size " + bytes.length + " only read " + position);
                }
            } catch(Exception e) {
                fail("Exception while testing buffered read for " + bytes.length + " size: " + e.getMessage());
                continue;
            } finally {
                if ( bis != null ) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        // ignore
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
