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

import java.util.Date;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;import org.junit.Before;

public class DateDataTests {

	Date today;
    Date notToday;


    @Before    public void setUp() throws Exception {

        today = DateUtils.roundDate(new Date());
        notToday = DateUtils.roundDate(new Date(today.getTime() - today.getTime()/2));
    }


    @Test
    public void testGetData() {
        DateData data = new DateData(today);
        assertEquals("DateData's getValue returned an incorrect date", data.getValue(), today);
        Date temp = new Date(today.getTime());
        today.setTime(1234);
        assertEquals("DateData's getValue was mutated incorrectly", data.getValue(), temp);

        Date rep = (Date)data.getValue();
        rep.setTime(rep.getTime() - 1000);

        assertEquals("DateData's getValue was mutated incorrectly", data.getValue(), temp);
    }
    @Test
    public void testSetData() {
        DateData data = new DateData(notToday);
        data.setValue(today);

        assertTrue("DateData did not set value properly. Maintained old value.", !(data.getValue().equals(notToday)));
        assertEquals("DateData did not properly set value ", data.getValue(), today);

        data.setValue(notToday);
        assertTrue("DateData did not set value properly. Maintained old value.", !(data.getValue().equals(today)));
        assertEquals("DateData did not properly reset value ", data.getValue(), notToday);

        Date temp = new Date(notToday.getTime());
        notToday.setTime(notToday.getTime() - 1324);

        assertEquals("DateData's value was mutated incorrectly", data.getValue(), temp);
    }

    @Test
    public void testDisplay() {
        // We don't actually want this, because the Date's getDisplayText code should be moved to a library
    }

    @Test
    public void testNullData() {
        boolean exceptionThrown = false;
        DateData data = new DateData();
        data.setValue(today);
        try {
            data.setValue(null);
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        assertTrue("DateData failed to throw an exception when setting null data", exceptionThrown);
        assertTrue("DateData overwrote existing value on incorrect input", data.getValue().equals(today));
    }
}
