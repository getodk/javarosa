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
import static org.javarosa.core.model.utils.DateUtils.FORMAT_ISO8601;
import static org.javarosa.core.model.utils.DateUtils.format;
import static org.javarosa.core.model.utils.DateUtils.formatDateTime;
import static org.javarosa.core.model.utils.DateUtils.getXMLStringValue;
import static org.javarosa.core.model.utils.DateUtils.parseDateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.javarosa.core.model.utils.DateUtils.DateFields;
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
    public void sanity_check_iso_format_and_parse_back() {

        Stream.of(new Date(1300139579000L), new Date(0)).forEach(input ->
            assertThat(parseDateTime(formatDateTime(input, FORMAT_ISO8601)), is(input)));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Stream.of(new Date(1300139579000L), new Date(0)).forEach(input ->
            assertThat(parseDateTime(formatDateTime(input, FORMAT_ISO8601)), is(input)));

        TimeZone offsetTwoHours = TimeZone.getTimeZone("GMT+02");

        TimeZone.setDefault(offsetTwoHours);

        Stream.of(new Date(1300139579000L), new Date(0)).forEach(input ->
            assertThat(parseDateTime(formatDateTime(input, FORMAT_ISO8601)), is(input)));

        TimeZone offTwoHalf = TimeZone.getTimeZone("GMT+0230");

        TimeZone.setDefault(offTwoHalf);

        Stream.of(new Date(1300139579000L), new Date(0)).forEach(input ->
            assertThat(parseDateTime(formatDateTime(input, FORMAT_ISO8601)), is(input)));

        TimeZone offMinTwoHalf = TimeZone.getTimeZone("GMT-0230");

        TimeZone.setDefault(offMinTwoHalf);

        Stream.of(new Date(1300139579000L), new Date(0)).forEach(input ->
            assertThat(parseDateTime(formatDateTime(input, FORMAT_ISO8601)), is(input)));


    }

    @Test
    public void testFormatting() {
        class LangJanSun {
            private LangJanSun(String language, String january, String sunday) {
                this.language = language;
                this.january = january;
                this.sunday = sunday;
            }

            private String language;
            private String january;
            private String sunday;
        }

        LangJanSun langJanSuns[] = new LangJanSun[]{
            new LangJanSun("en", "Jan", "Sun"),
            new LangJanSun("es", "ene", "dom"),
            new LangJanSun("fr", "janv.", "dim.")
        };

        for (LangJanSun ljs : langJanSuns) {
            Locale.setDefault(Locale.forLanguageTag(ljs.language));

            String month = format(DateFields.of(2018, 1, 1, 10, 20, 30, 400), "%b");
            assertEquals(ljs.january, month);

            // 2018-04-01 was sunday
            String day = format(DateFields.of(2018, 4, 1, 10, 20, 30, 400), "%a");
            assertEquals(ljs.sunday, day);
        }
    }
}
