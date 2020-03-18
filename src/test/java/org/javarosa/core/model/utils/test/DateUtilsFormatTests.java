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

package org.javarosa.core.model.utils.test;

import static org.junit.Assert.assertEquals;


import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.DateUtils.DateFields;

import org.junit.Test;

public class DateUtilsFormatTests {
  
    @Test
    public void testFormatWeekOfYear() {
        String week = DateUtils.format(DateFields.of(2018, 4, 1, 10, 20, 30, 400), "%W");
        assertEquals("13", week);
        
        week = DateUtils.format(DateFields.of(2018, 1, 1, 10, 20, 30, 400), "%W");
        assertEquals("01", week);

        // Week of year is based on what year the first thursday is. 1/1/2017 was a Sunday so 
        // it's actually the 52nd week of the previous year.
        week = DateUtils.format(DateFields.of(2017, 1, 1, 10, 20, 30, 400), "%W");
        assertEquals("52", week);
       
    }
    
}
