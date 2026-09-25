package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.Scenario.AnswerResult.CONSTRAINT_VIOLATED;
import static org.javarosa.test.Scenario.AnswerResult.OK;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import java.time.Instant;

import org.javarosa.test.Scenario;
import org.junit.Test;

public class ConstraintTest {

    @Test
    public void dateTimeConstraint_withEarlierTime_isSatisfied() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". <= /data/reference");
        assertThat(scenario.answer(Instant.parse("2021-06-15T11:00:00Z")), is(OK));
    }

    @Test
    public void dateTimeConstraint_withSameTime_isSatisfied() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". <= /data/reference");
        assertThat(scenario.answer(Instant.parse("2021-06-15T12:00:00Z")), is(OK));
    }

    @Test
    public void dateTimeConstraint_withLaterTime_isViolated() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". <= /data/reference");
        assertThat(scenario.answer(Instant.parse("2021-06-15T13:00:00Z")), is(CONSTRAINT_VIOLATED));
    }

    @Test
    public void dateTimeConstraint_withLaterDate_isViolated() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". <= /data/reference");
        assertThat(scenario.answer(Instant.parse("2021-06-16T11:00:00Z")), is(CONSTRAINT_VIOLATED));
    }

    @Test
    public void dateTimeConstraint_withSameTime_greaterThanOrEqual_isSatisfied() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". >= /data/reference");
        assertThat(scenario.answer(Instant.parse("2021-06-15T12:00:00Z")), is(OK));
    }

    @Test
    public void dateTimeConstraint_withEarlierTime_greaterThanOrEqual_isViolated() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". >= /data/reference");
        assertThat(scenario.answer(Instant.parse("2021-06-15T11:00:00Z")), is(CONSTRAINT_VIOLATED));
    }

    @Test
    public void dateTimeConstraint_withFutureTime_comparedToNow_isViolated() throws Exception {
        Scenario scenario = scenarioWithDateTimeConstraint(". <= now()");
        assertThat(scenario.answer(Instant.now().plusSeconds(3600)), is(CONSTRAINT_VIOLATED));
    }

    private Scenario scenarioWithDateTimeConstraint(String constraint) throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("DateTime constraint"),
                model(
                    mainInstance(
                        t("data id='datetime'",
                            t("date_time"),
                            t("reference", "2021-06-15T12:00:00.000Z")
                        )
                    ),
                    bind("/data/date_time").type("dateTime").constraint(constraint),
                    bind("/data/reference").type("dateTime")
                )
            ),
            body(input("/data/date_time"))
        ));

        scenario.next();
        return scenario;
    }
}