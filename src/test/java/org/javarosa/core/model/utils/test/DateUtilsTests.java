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

import static java.time.DayOfWeek.SUNDAY;
import static java.time.Month.JANUARY;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateUtils.getXMLStringValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;

public class DateUtilsTests {
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
            private LangJanSun(Locale locale) {
                this.locale = locale;
            }

            private Locale locale;
        }

        LangJanSun langJanSuns[] = new LangJanSun[]{
            new LangJanSun(Locale.ENGLISH),
            new LangJanSun(Locale.forLanguageTag("es-ES")),
            new LangJanSun(Locale.FRENCH)
        };

        for (LangJanSun ljs : langJanSuns) {
            withLocale(ljs.locale, locale -> {
                String expectedJanuary = JANUARY.getDisplayName(TextStyle.SHORT, locale);
                String expectedSunday = SUNDAY.getDisplayName(TextStyle.SHORT, locale);

                // Use a Sunday in January for our test
                LocalDateTime localDateTime = LocalDateTime.parse("2018-01-07T10:20:30.400");
                Date date = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));

                String month = DateUtils.format(date, "%b");
                assertEquals(expectedJanuary, month);

                String day = DateUtils.format(date, "%a");
                assertEquals(expectedSunday, day);
            });
        }
    }

    public void withLocale(Locale locale, Consumer<Locale> block) {
        Locale backupLocale = Locale.getDefault();
        Locale.setDefault(locale);
        block.accept(locale);
        Locale.setDefault(backupLocale);
    }
}
