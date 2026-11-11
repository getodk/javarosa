package org.javarosa.core.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;

import java.time.Instant;
import java.util.TimeZone;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.test.Scenario;
import org.junit.Test;

public class DateTimeTest {
    @Test
    public void dateTimeArithmetic_preservesDateTimeType() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
        Scenario scenario;
            scenario = Scenario.init(html(
                head(title("DateTime arithmetic"),
                    model(
                        mainInstance(t("data id=\"datetime-arithmetic\"",
                            t("dateTime"),
                            t("nextDateTime")
                        )),
                        bind("/data/dateTime").type("dateTime"),
                        bind("/data/nextDateTime").type("dateTime").calculate("/data/dateTime + 1")
                    )
                ),
                body(
                    input("/data/dateTime")
                )
            ));

            scenario.answer("/data/dateTime", Instant.parse("2021-11-30T12:00:00Z"));

            assertThat(scenario.answerOf("/data/nextDateTime"), instanceOf(DateTimeData.class));
            assertThat(scenario.answerOf("/data/nextDateTime").uncast().getValue(),
                is("2021-12-01T12:00:00.000Z"));
        });
    }

    @Test
    public void dateTimeQuestionReturnsDateTimeDataAnswer() {
        Scenario scenario = Scenario.init(html(
            head(
                title("DateTime form"),
                model(
                    mainInstance(t("data id=\"datetime-form\"",
                        t("calculateLiteral"),
                        t("empty", "2025-09-25T23:15:00.000+02:00"),
                        t("calculateReference")
                    )),
                    bind("/data/calculateLiteral").type("dateTime").calculate("&quot;2025-09-25T23:15:00.000+02:00&quot;"),
                    bind("/data/empty").type("dateTime"),
                    bind("/data/calculateReference").type("dateTime").calculate("/data/empty")
                )
            ),
            body(
                input("/data/calculateLiteral"),
                input("/data/empty"),
                input("/data/calculateReference")
            )
        ));
        IAnswerData answer1 = scenario.answerOf("/data/calculateLiteral");
        assertThat(answer1 instanceof DateTimeData, equalTo(true));

        IAnswerData answer2 = scenario.answerOf("/data/empty");
        assertThat(answer2 instanceof DateTimeData, equalTo(true));

        IAnswerData answer3 = scenario.answerOf("/data/calculateReference");
        assertThat(answer3 instanceof DateTimeData, equalTo(true));
    }

    @Test
    public void dateTimeQuestionWithInvalidDateTimeFormatResultsInNoAnswer() {
        Scenario scenario = Scenario.init(html(
            head(
                title("DateTime form"),
                model(
                    mainInstance(t("data id=\"datetime-form\"",
                        t("calculateLiteral"),
                        t("empty", "not-a-datetime"),
                        t("calculateReference")
                    )),
                    bind("/data/calculateLiteral").type("dateTime").calculate("&quot;not-a-datetime&quot;"),
                    bind("/data/empty").type("dateTime"),
                    bind("/data/calculateReference").type("dateTime").calculate("/data/empty")
                )
            ),
            body(
                input("/data/calculateLiteral"),
                input("/data/empty"),
                input("/data/calculateReference")
            )
        ));

        assertThat(scenario.answerOf("/data/calculateLiteral"), nullValue());
        assertThat(scenario.answerOf("/data/empty"), nullValue());
        assertThat(scenario.answerOf("/data/calculateReference"), nullValue());
    }

    @Test
    public void dateField_withDateTimeDefault_truncatesTime() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            Scenario scenario = Scenario.init(html(
                head(
                    title("Date question with datetime default"),
                    model(
                        mainInstance(t("data id=\"datetime_default\"",
                            t("date", "2025-01-14T03:43:58+00:00")
                        )),
                        bind("/data/date").type("date")
                    )
                ),
                body(
                    input("/data/date")
                )));

            // Would be 2025-01-13 in America/Los_Angeles so this shows time is truncated without TZ conversion
            assertThat(scenario.answerOf("/data/date").uncast().getValue(), is("2025-01-14"));
        });
    }

    /**
     * Documents a limitation of using local epoch as reference for datetime.
     * Warsaw was UTC+1 on January 1, 1970. When the offset matches, midnight arithmetic is continuous. When it
     * differs, midnight is treated as a date while the following millisecond is treated as a datetime, producing
     * a discontinuity.
     */
    @Test
    public void dateTimeArithmetic_atMidnight_dependsOnEpochTimezoneOffset() {
        withTimeZone(TimeZone.getTimeZone("Europe/Warsaw"), () -> {
            Scenario scenario = Scenario.init(html(
                head(
                    title("Datetime arithmetic"),
                    model(
                        mainInstance(t("data id=\"datetime-arithmetic\"",
                            t("winterMidnight", "2026-01-15T00:00:00.000"),
                            t("winterAfterMidnight", "2026-01-15T00:00:00.001"),
                            t("winterDifference"),
                            t("summerMidnight", "2026-07-15T00:00:00.000"),
                            t("summerAfterMidnight", "2026-07-15T00:00:00.001"),
                            t("summerDifference")
                        )),
                        bind("/data/winterMidnight").type("dateTime"),
                        bind("/data/winterAfterMidnight").type("dateTime"),
                        bind("/data/winterDifference").type("decimal").calculate("/data/winterAfterMidnight - /data/winterMidnight"),
                        bind("/data/summerMidnight").type("dateTime"),
                        bind("/data/summerAfterMidnight").type("dateTime"),
                        bind("/data/summerDifference").type("decimal").calculate("/data/summerAfterMidnight - /data/summerMidnight")
                    )
                ),
                body(
                    input("/data/winterMidnight"),
                    input("/data/winterAfterMidnight"),
                    input("/data/summerMidnight"),
                    input("/data/summerAfterMidnight")
                )
            ));

            double winterDifference = Double.parseDouble(scenario.answerOf("/data/winterDifference").uncast().getDisplayText());
            double summerDifference = Double.parseDouble(scenario.answerOf("/data/summerDifference").uncast().getDisplayText());

            // Winter: UTC+1 matches the epoch offset, so the difference is the expected one millisecond, expressed in days.
            assertThat(winterDifference, is(closeTo(1.0 / 86400000, 1e-9)));

            // Summer: UTC+2 differs from the epoch offset by one hour. The numeric value decreases by approximately
            // one hour instead of increasing by one millisecond.
            assertThat(summerDifference, is(closeTo(-1.0 / 24, 1e-6)));
        });
    }
}
