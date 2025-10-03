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
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

public class DateTimeTest {
    @Test
    public void timeQuestionReturnsTimeDataAnswer() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("DateTime form", html(
            head(
                title("Time form"),
                model(
                    mainInstance(t("data id=\"time-form\"",
                        t("calculateLiteral"),
                        t("empty", "23:14:00.000+02:00"),
                        t("calculateReference")
                    )),
                    bind("/data/calculateLiteral").type("time").calculate("&quot;23:14:00.000+02:00&quot;"),
                    bind("/data/empty").type("time"),
                    bind("/data/calculateReference").type("time").calculate("/data/empty")
                )
            ),
            body(
                input("/data/calculateLiteral"),
                input("/data/empty"),
                input("/data/calculateReference")
            )
        ));
        IAnswerData answer1 = scenario.answerOf("/data/calculateLiteral");
        assertThat(answer1 instanceof TimeData, equalTo(true));

        IAnswerData answer2 = scenario.answerOf("/data/empty");
        assertThat(answer2 instanceof TimeData, equalTo(true));

        IAnswerData answer3 = scenario.answerOf("/data/calculateReference");
        assertThat(answer3 instanceof TimeData, equalTo(true));
    }

    @Test
    public void dateQuestionReturnsDateDataAnswer() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("DateTime form", html(
            head(
                title("Date form"),
                model(
                    mainInstance(t("data id=\"date-form\"",
                        t("calculateLiteral"),
                        t("empty", "2025-09-25"),
                        t("calculateReference")
                    )),
                    bind("/data/calculateLiteral").type("date").calculate("&quot;2025-09-25&quot;"),
                    bind("/data/empty").type("date"),
                    bind("/data/calculateReference").type("date").calculate("/data/empty")
                )
            ),
            body(
                input("/data/calculateLiteral"),
                input("/data/empty"),
                input("/data/calculateReference")
            )
        ));
        IAnswerData answer1 = scenario.answerOf("/data/calculateLiteral");
        assertThat(answer1 instanceof DateData, equalTo(true));

        IAnswerData answer2 = scenario.answerOf("/data/empty");
        assertThat(answer2 instanceof DateData, equalTo(true));

        IAnswerData answer3 = scenario.answerOf("/data/calculateReference");
        assertThat(answer3 instanceof DateData, equalTo(true));
    }

    @Test
    public void dateTimeQuestionReturnsDateTimeDataAnswer() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("DateTime form", html(
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
}
