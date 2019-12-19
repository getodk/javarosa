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

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.javarosa.core.io.BufferedInputStream;
import org.javarosa.core.util.ArrayUtilities;import org.junit.Before;
import org.junit.Test;

public class BufferedInputStreamTests {

    byte[] bytes;

    int[] sizesToTest = new int[] { 15, 64, 500, 1280, 2047, 2048, 2049, 5000, 10000, 23000};

    byte[][] arraysToTest;


    @Before    public void setUp() throws Exception {


        Random r = new Random();

        arraysToTest = new byte[sizesToTest.length][];
        for(int i = 0 ; i < sizesToTest.length ; ++i) {
            arraysToTest[i] = new byte[sizesToTest[i]];
            r.nextBytes(arraysToTest[i]);
        }
    }

    @Test
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

    @Test
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
