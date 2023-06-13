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

import org.javarosa.core.model.utils.DateFields;
import org.javarosa.core.model.utils.DateFormatter;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateUtilsFormatTests {
    @Test
    public void testFormatWeekOfYearUsingPatterns() {
        String week = DateFormatter.format(DateUtils.getDate(DateFields.of(2018, 4, 1, 10, 20, 30, 400)), "%W");
        assertEquals("13", week);

        week = DateFormatter.format(DateUtils.getDate(DateFields.of(2018, 1, 1, 10, 20, 30, 400)), "%W");
        assertEquals("1", week);

        // Week of year is based on what year the first thursday is. 1/1/2017 was a Sunday so
        // it's actually the 52nd week of the previous year. And in ISO speak, week 0 in the new year
        week = DateFormatter.format(DateUtils.getDate(DateFields.of(2016, 12, 31, 10, 20, 30, 400)), "%W");
        assertEquals("52", week);
        week = DateFormatter.format(DateUtils.getDate(DateFields.of(2017, 1, 1, 10, 20, 30, 400)), "%W");
        assertEquals("52", week);
        week = DateFormatter.format(DateUtils.getDate(DateFields.of(2017, 1, 2, 10, 20, 30, 400)), "%W");
        assertEquals("1", week);

        // 12/29/2020 is the 53rd week of the year
        week = DateFormatter.format(DateUtils.getDate(DateFields.of(2020, 12, 29, 10, 20, 30, 400)), "%W");
        assertEquals("53", week);
        // 1/1/2021, as it's in the 53rd week of the previous year, is oth week of the new year
        week = DateFormatter.format(DateUtils.getDate(DateFields.of(2021, 1, 1, 10, 20, 30, 400)), "%W");
        assertEquals("53", week);
    }
}
