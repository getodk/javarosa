package org.javarosa.core.model;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.*;
import static org.javarosa.test.XFormsElement.input;

public class DateTimeTest {
    @Test
    public void timeQuestionReturnsTimeDataAnswer() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("DateTime form", html(
            head(
                title("Time form"),
                model(
                    mainInstance(t("data id=\"time-form\"",
                        t("time1"),
                        t("time2", "23:14:00.000+02:00"),
                        t("time3")
                    )),
                    bind("/data/time1").type("time").calculate("&quot;23:14:00.000+02:00&quot;"),
                    bind("/data/time2").type("time"),
                    bind("/data/time3").type("time").calculate("/data/time2")
                )
            ),
            body(
                input("/data/time1"),
                input("/data/time2"),
                input("/data/time3")
            )
        ));
        IAnswerData answer1 = scenario.answerOf("/data/time1");
        assertThat(answer1 instanceof TimeData, equalTo(true));

        IAnswerData answer2 = scenario.answerOf("/data/time2");
        assertThat(answer2 instanceof TimeData, equalTo(true));

        IAnswerData answer3 = scenario.answerOf("/data/time3");
        assertThat(answer3 instanceof TimeData, equalTo(true));
    }

    @Test
    public void dateQuestionReturnsDateDataAnswer() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("DateTime form", html(
            head(
                title("Date form"),
                model(
                    mainInstance(t("data id=\"date-form\"",
                        t("date1"),
                        t("date2", "2025-09-25"),
                        t("date3")
                    )),
                    bind("/data/date1").type("date").calculate("&quot;2025-09-25&quot;"),
                    bind("/data/date2").type("date"),
                    bind("/data/date3").type("date").calculate("/data/date2")
                )
            ),
            body(
                input("/data/date1"),
                input("/data/date2"),
                input("/data/date3")
            )
        ));
        IAnswerData answer1 = scenario.answerOf("/data/date1");
        assertThat(answer1 instanceof DateData, equalTo(true));

        IAnswerData answer2 = scenario.answerOf("/data/date2");
        assertThat(answer2 instanceof DateData, equalTo(true));

        IAnswerData answer3 = scenario.answerOf("/data/date3");
        assertThat(answer3 instanceof DateData, equalTo(true));
    }

    @Test
    public void dateTimeQuestionReturnsDateTimeDataAnswer() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("DateTime form", html(
            head(
                title("DateTime form"),
                model(
                    mainInstance(t("data id=\"datetime-form\"",
                        t("dateTime1"),
                        t("dateTime2", "2025-09-25T23:15:00.000+02:00"),
                        t("dateTime3")
                    )),
                    bind("/data/dateTime1").type("dateTime").calculate("&quot;2025-09-25T23:15:00.000+02:00&quot;"),
                    bind("/data/dateTime2").type("dateTime"),
                    bind("/data/dateTime3").type("dateTime").calculate("/data/dateTime2")
                )
            ),
            body(
                input("/data/dateTime1"),
                input("/data/dateTime2"),
                input("/data/dateTime3")
            )
        ));
        IAnswerData answer1 = scenario.answerOf("/data/dateTime1");
        assertThat(answer1 instanceof DateTimeData, equalTo(true));

        IAnswerData answer2 = scenario.answerOf("/data/dateTime2");
        assertThat(answer2 instanceof DateTimeData, equalTo(true));

        IAnswerData answer3 = scenario.answerOf("/data/dateTime3");
        assertThat(answer3 instanceof DateTimeData, equalTo(true));
    }
}
