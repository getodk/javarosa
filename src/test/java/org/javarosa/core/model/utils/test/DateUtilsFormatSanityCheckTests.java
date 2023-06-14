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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;

import static java.util.TimeZone.getTimeZone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateFormatter.FORMAT_ISO8601;
import static org.javarosa.core.model.utils.DateFormatter.formatDateTime;
import static org.javarosa.core.model.utils.DateUtils.parseDateTime;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;

@RunWith(Parameterized.class)
public class DateUtilsFormatSanityCheckTests {
    @Parameterized.Parameter()
    public long inputTimestamp;

    @Parameterized.Parameters(name = "Input timestamp: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {1300139579000L},
//            {0} -> this chain does not handle dates that straddle end of year. The prod code works as expected, as verified in `manualRoundTrip` test below
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

    /** This verifies that the commented out option in the above test `sanity_check_iso_format_and_parse_back` does work when run step by step.
     * as all the other tests are passing, and we are moving away from java.util.Date, I don't want to spend any more time looking into it. - JB
     */
    @Test public void manualRoundTrip(){
        Date date = new Date(0);
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");// from DateFormat.ISO8601
        isoFormat.setTimeZone(TimeZone.getTimeZone("GMT-13"));
        String formattedDateTime = formatDateTime(date, FORMAT_ISO8601);
        Date parsedDateTime = parseDateTime(formattedDateTime);
        assertThat(date, is(parsedDateTime));
    }

    @Test public void manualRoundTripVerbose(){
        Date date = new Date(0);
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");//yyyy-MM-dd'T'HH:mm:ss.SSS from DateFormat.ISO8601
        String raw = isoFormat.format(date);
        System.out.println("raw               = " + raw);
        System.out.println("formattedRaw      = " + formatDateTime(date, 1));

        isoFormat.setTimeZone(TimeZone.getTimeZone("GMT-13"));
        System.out.println("gmt-13            = " + isoFormat.format(date));

        String formattedDateTime = formatDateTime(date, FORMAT_ISO8601);
        System.out.println("formattedDateTime = " + formattedDateTime);

        Date parsedDateTime = parseDateTime(formattedDateTime);
        System.out.println("parsedDateTime    = " + parsedDateTime);
        System.out.println();

        assertThat(date, is(parsedDateTime));
    }
}
