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
import java.time.temporal.IsoFields;
import java.util.Date;

import static org.javarosa.core.model.utils.DateFormatter.format;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDateTime;
import static org.junit.Assert.assertEquals;

public class DateUtilsFormatTests {
    @Test
    public void testFormatWeekOfYearUsingPatterns() {
        String week = format(dateFromDateTime(2018, 4, 1), "%W");
        assertEquals("13", week);

        week = format(dateFromDateTime(2018, 1, 1), "%W");
        assertEquals("1", week);

        // Week of year is based on what year the first thursday is. 1/1/2017 was a Sunday so
        // it's actually the 52nd week of the previous year. And in ISO speak, week 0 in the new year
        week = format(dateFromDateTime(2016, 12, 31), "%W");
        assertEquals("52", week);
        week = format(dateFromDateTime(2017, 1, 1), "%W");
        assertEquals("52", week);
        week = format(dateFromDateTime(2017, 1, 2), "%W");
        assertEquals("1", week);

        // 12/29/2020 is the 53rd week of the year
        week = format(dateFromDateTime(2020, 12, 29), "%W");
        assertEquals("53", week);
        // 1/1/2021, as it's in the 53rd week of the previous year, is oth week of the new year
        week = format(dateFromDateTime(2021, 1, 1), "%W");
        assertEquals("53", week);
    }

    @Test public  void testWeeksInDateTime() {
        int week = weekOfLocalDate(asLocalDateTime(2018, 4, 1));
        assertEquals(13, week);

        week = weekOfLocalDate(asLocalDateTime(2018, 1, 1));
        assertEquals(1, week);

        // Week of year is based on what year the first thursday is. 1/1/2017 was a Sunday so
        // it's actually the 52nd week of the previous year. And in ISO speak, week 0 in the new year
        week = weekOfLocalDate(asLocalDateTime(2016, 12, 31));
        assertEquals(52, week);
        week = weekOfLocalDate(asLocalDateTime(2017, 1, 1));
        assertEquals(52, week);
        week = weekOfLocalDate(asLocalDateTime(2017, 1, 2));
        assertEquals(1, week);

        // 12/29/2020 is the 53rd week of the year
        week = weekOfLocalDate(asLocalDateTime(2020, 12, 29));
        assertEquals(53, week);
        // 1/1/2021, as it's in the 53rd week of the previous year, is oth week of the new year
        week = weekOfLocalDate(asLocalDateTime(2021, 1, 1));
        assertEquals(53, week);

    }

    @NotNull
    private Date dateFromDateTime(int year, int month, int month1) {
        LocalDateTime localDateTime = asLocalDateTime(year, month, month1);
        return dateFromLocalDateTime(localDateTime, ZoneId.of("UTC"));
    }

    @NotNull
    private static LocalDateTime asLocalDateTime(int year, int month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);
        LocalTime localTime = LocalTime.of(10, 20, 30);
        return LocalDateTime.of(localDate, localTime);
    }

    private int weekOfLocalDate(LocalDateTime localDateTime) {
        return ZonedDateTime
                .of(localDateTime, ZoneId.of ( "UTC" ))
                .get ( IsoFields.WEEK_OF_WEEK_BASED_YEAR );
    }

}
