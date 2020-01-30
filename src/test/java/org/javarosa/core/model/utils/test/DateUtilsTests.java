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

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateUtils.getXMLStringValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DateUtilsTests {
    private Locale backupLocale;
    private TimeZone backupZone;

    @Before
    public void setUp() {
        backupLocale = Locale.getDefault();
        backupZone = TimeZone.getDefault();

    }

    @After
    public void tearDown() {
        TimeZone.setDefault(backupZone);
        Locale.setDefault(backupLocale);
    }

    /**
     * This test ensures that the Strings returned
     * by the getXMLStringValue function are in
     * the proper XML compliant format, which can be
     * parsed by LocalDate.parse()
     */
    @Test
    public void testGetXMLStringValueFormat() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        Date nowDate = Date.from(nowDateTime.toInstant(OffsetDateTime.now().getOffset()));
        String nowXmlFormatterDate = getXMLStringValue(nowDate);
        assertThat(LocalDate.parse(nowXmlFormatterDate), is(nowDateTime.toLocalDate()));
    }

    @Test
    public void format_is_localized() {
        class LangJanSun {
            private LangJanSun(Locale locale, String january, String sunday) {
                this.locale = locale;
                this.january = january;
                this.sunday = sunday;
            }

            private Locale locale;
            private String january;
            private String sunday;
        }

        LangJanSun langJanSuns[] = new LangJanSun[]{
            new LangJanSun(Locale.ENGLISH, Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)),
            new LangJanSun(Locale.forLanguageTag("es-ES"), Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-ES")), DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-ES"))),
            new LangJanSun(Locale.FRENCH, Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.FRENCH), DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, Locale.FRENCH))
        };

        for (LangJanSun ljs : langJanSuns) {
            Locale.setDefault(ljs.locale);

            // Use a Sunday in January for our test
            LocalDateTime localDateTime = LocalDateTime.parse("2018-01-07T10:20:30.400");
            Date date = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));

            String month = DateUtils.format(date, "%b");
            assertEquals(ljs.january, month);

            String day = DateUtils.format(date, "%a");
            assertEquals(ljs.sunday, day);
        }
    }
}
