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

import static java.util.TimeZone.getTimeZone;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DateUtilsParseDateTimeTests {
    @Parameterized.Parameter()
    public String input;

    @Parameterized.Parameter(value = 1)
    public Temporal expectedDateTime;

    @Parameterized.Parameters(name = "Input: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"2016-04-13T16:26:00.000", LocalDateTime.parse("2016-04-13T16:26:00.000")},
            {"2016-04-13T16:26:00.000-07", OffsetDateTime.parse("2016-04-13T16:26:00.000-07:00")},
            {"2015-12-16T16:09:00.000-08", OffsetDateTime.parse("2015-12-16T16:09:00.000-08:00")},
            {"2015-12-16T07:09:00.000+08", OffsetDateTime.parse("2015-12-16T07:09:00.000+08:00")},
            {"2015-11-30T16:09:00.000-08", OffsetDateTime.parse("2015-11-30T16:09:00.000-08:00")},
            {"2015-11-01T07:09:00.000+08", OffsetDateTime.parse("2015-11-01T07:09:00.000+08:00")},
            {"2015-12-31T16:09:00.000-08", OffsetDateTime.parse("2015-12-31T16:09:00.000-08:00")},
        });
    }

    @Test
    public void parseDateTime_produces_expected_results_in_all_time_zones() {
        Stream.of(
            TimeZone.getDefault(),
            getTimeZone("UTC"),
            getTimeZone("GMT+12"),
            getTimeZone("GMT-13"),
            getTimeZone("GMT+0230")
        ).forEach(tz -> withTimeZone(tz, () -> assertThat(parseDateTime(input), is(expectedDateTime))));
    }

    /**
     * Returns a LocalDateTime or an OffsetTimeTime obtained from the result of
     * calling DateUtils.parseDateTime() with the provided input, ensuring that
     * both represent the same instant.
     */
    private Temporal parseDateTime(String input) {
        Instant inputInstant = Objects.requireNonNull(DateUtils.parseDateTime(input)).toInstant();

        String timePart = input.substring(11);
        if (timePart.contains("+") || timePart.contains("-")) {
            // The input declares some positive or negative time offset
            int beginOfOffsetPart = timePart.contains("+") ? timePart.indexOf("+") : timePart.indexOf("-");
            String offsetPart = timePart.substring(beginOfOffsetPart);
            // The input declares some positive or negative time offset
            String offset = offsetPart.length() == 3 ? offsetPart + ":00" : offsetPart;
            return OffsetDateTime.ofInstant(inputInstant, ZoneId.of(offset));
        }

        if (input.endsWith("Z"))
            // The input time is at UTC
            return OffsetDateTime.ofInstant(inputInstant, ZoneId.of("Z"));

        // No time offset declared. Return a LocalTime
        return LocalDateTime.ofInstant(inputInstant, ZoneId.systemDefault());
    }
}
