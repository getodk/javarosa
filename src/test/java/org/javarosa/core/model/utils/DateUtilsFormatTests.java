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
package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class DateUtilsFormatTests {

    @Test
    public void testFormatWeekOfYearUsingPatternsAndDateTime() {
        DateTimeFormatter weekFormatter = DateFormatter.formatterFrom("%W");

        assertEquals("13", weekFormatter.format(zonedDateTime(2018, 4, 1)));

        assertEquals("1", weekFormatter.format(zonedDateTime(2018, 1, 1)));

        // Week of year is based on what year the first thursday is. 1/1/2017 was a Sunday so
        // it's actually the 52nd week of the previous year. And in ISO speak, week 0 in the new year
        assertEquals("52", weekFormatter.format(zonedDateTime(2016, 12, 31)));
        assertEquals("52", weekFormatter.format(zonedDateTime(2017, 1, 1)));
        assertEquals("1", weekFormatter.format(zonedDateTime(2017, 1, 2)));

        // 12/29/2020 is the 53rd week of the year
        assertEquals("53", weekFormatter.format(zonedDateTime(2020, 12, 29)));
        // 1/1/2021, as it's in the 53rd week of the previous year, is oth week of the new year
        assertEquals("53", weekFormatter.format(zonedDateTime(2021, 1, 1)));
    }

    @NotNull
    private static ZonedDateTime zonedDateTime(int year, int month, int day) {
        return ZonedDateTime
                .of(asLocalDateTime(year, month, day),
                        ZoneId.of("UTC"));
    }

    @NotNull
    private static LocalDateTime asLocalDateTime(int year, int month, int day) {
        return LocalDateTime
                .of(LocalDate.of(year, month, day),
                        LocalTime.of(10, 20, 30));
    }
}
