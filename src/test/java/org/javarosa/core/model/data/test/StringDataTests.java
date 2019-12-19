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

package org.javarosa.core.model.data.test;




import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.javarosa.core.model.data.StringData;import org.junit.Before;import org.junit.Test;

public class StringDataTests {
    String stringA;
    String stringB;


    /* (non-Javadoc)
     * @see j2meunit.framework.TestCase#setUp()
     */
    @Before    public void setUp() throws Exception {


        stringA = "string A";
        stringB = "string B";
    }

    @Test
    public void testGetData() {
        StringData data = new StringData(stringA);
        assertEquals("StringData's getValue returned an incorrect String", data.getValue(), stringA);

    }
    @Test
    public void testSetData() {
        StringData data = new StringData(stringA);
        data.setValue(stringB);

        assertTrue("StringData did not set value properly. Maintained old value.", !(data.getValue().equals(stringA)));
        assertEquals("StringData did not properly set value ", data.getValue(), stringB);

        data.setValue(stringA);
        assertTrue("StringData did not set value properly. Maintained old value.", !(data.getValue().equals(stringB)));
        assertEquals("StringData did not properly reset value ", data.getValue(), stringA);
    }
    @Test
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
