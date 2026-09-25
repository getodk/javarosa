package org.javarosa.core.model.utils.test;

import static java.util.TimeZone.getTimeZone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DateUtilsParseDateTests {
    @Parameterized.Parameter(value = 0)
    public String input;

    @Parameterized.Parameter(value = 1)
    public LocalDate expectedDate;

    @Parameterized.Parameters(name = "Input: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"2016-04-13", LocalDate.parse("2016-04-13")},
            {"2016-04-13T16:26:00.000", LocalDate.parse("2016-04-13")},
            {"2016-04-13T16:26:00.000Z", LocalDate.parse("2016-04-13")},
            {"2016-04-13T16:26:00.000-07", LocalDate.parse("2016-04-13")},
            {"2016-04-13T16:26:00.000+08", LocalDate.parse("2016-04-13")},
            {"2016-04-13T00:30:00.000+12", LocalDate.parse("2016-04-13")},
            {"2016-04-13T23:30:00.000-13", LocalDate.parse("2016-04-13")},
        });
    }

    @Test
    public void parseDate_produces_expected_results_in_all_time_zones() {
        Stream.of(
            TimeZone.getDefault(),
            getTimeZone("UTC"),
            getTimeZone("GMT+12"),
            getTimeZone("GMT-13"),
            getTimeZone("GMT+0230")
        ).forEach(tz -> withTimeZone(tz, () -> assertThat(parseDate(input), is(expectedDate))));
    }

    private LocalDate parseDate(String input) {
        Date date = DateUtils.parseDate(input);
        return date == null
            ? null
            : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
