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
import static org.javarosa.core.model.utils.DateFormatter.FORMAT_ISO8601;
import static org.javarosa.core.model.utils.DateFormatter.formatDateTime;
import static org.javarosa.core.model.utils.DateUtils.parseDateTime;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DateUtilsFormatSanityCheckTests {
    @Parameterized.Parameter()
    public long inputTimestamp;

    @Parameterized.Parameters(name = "Input timestamp: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {1300139579000L},
            {0}
        });
    }

    @Test
    public void sanity_check_iso_format_and_parse_back() {
        Date input = new Date(inputTimestamp);
        Stream.of(
            TimeZone.getDefault(),
            getTimeZone("UTC"),
            getTimeZone("GMT+12"),
            getTimeZone("GMT-13"),
            getTimeZone("GMT+0230")
        ).forEach(timeZone -> withTimeZone(timeZone, () ->
            assertThat(parseDateTime(formatDateTime(input, FORMAT_ISO8601)), is(input))));
    }
}
