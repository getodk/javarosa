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
import static java.time.format.TextStyle.SHORT;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.utils.SystemHelper.withLocale;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

import org.javarosa.core.model.utils.DateFormatter;
import org.junit.Test;

public class DateUtilsFormatLocalizationTests {
    @Test
    public void format_is_localized() {
        // Use a Sunday in January for our test
        LocalDateTime localDateTime = LocalDateTime.parse("2018-01-07T10:20:30.400");
        Date date = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));

        Stream.of(
            Locale.ENGLISH,
            Locale.forLanguageTag("es-ES"),
            Locale.FRENCH
        ).forEach(locale -> withLocale(locale, l -> {
            assertThat(DateFormatter.format(date, "%b"), is(JANUARY.getDisplayName(SHORT, l)));
            assertThat(DateFormatter.format(date, "%a"), is(SUNDAY.getDisplayName(SHORT, l)));
        }));
    }

}
