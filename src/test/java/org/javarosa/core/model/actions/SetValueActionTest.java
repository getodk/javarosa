package org.javarosa.core.model.actions;

import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.test.AnswerDataMatchers.dateTimeAnswer;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class SetValueActionTest {
    private static final long DATE_NOW = 1_500_000_000_000L;
    private static final long DAY_OFFSET = 86_400_000L;

    @Before
    public void before() {
        DateTimeUtils.setCurrentMillisFixed(DATE_NOW);
    }

    @After
    public void after() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void when_triggerNodeIsUpdated_targetNodeCalculation_isEvaluated() throws IOException {
        Scenario scenario = Scenario.init("Nested setvalue action", html(
            head(
                title("Nested setvalue action"),
                model(
                    mainInstance(t("data id=\"nested-setvalue\"",
                        t("source"),
                        t("destination")
                    )),
                    bind("/data/source").type("int"),
                    bind("/data/destination").type("dateTime")
                )
            ),
            body(
                input("/data/source",
                    t("setvalue event=\"xforms-value-changed\" ref=\"/data/destination\" value=\"now()\""))
            )));

        assertThat(scenario.answerOf("/data/destination"), is(nullValue()));

        scenario.next();
        scenario.answer(22);

        assertThat(scenario.answerOf("/data/destination"), is(dateTimeAnswer(DATE_NOW)));
    }

    @Test
    public void when_triggerNodeIsUpdatedWithTheSameValue_targetNodeCalculation_isNotEvaluated() throws IOException {
        Scenario scenario = Scenario.init("Nested setvalue action", html(
            head(
                title("Nested setvalue action"),
                model(
                    mainInstance(t("data id=\"nested-setvalue\"",
                        t("source"),
                        t("destination")
                    )),
                    bind("/data/source").type("int"),
                    bind("/data/destination").type("dateTime")
                )
            ),
            body(
                input("/data/source",
                    t("setvalue event=\"xforms-value-changed\" ref=\"/data/destination\" value=\"now()\""))
            )));

        assertThat(scenario.answerOf("/data/destination"), is(nullValue()));

        scenario.next();
        scenario.answer(22);

        assertThat(scenario.answerOf("/data/destination"), is(dateTimeAnswer(DATE_NOW)));

        DateTimeUtils.setCurrentMillisFixed(DATE_NOW + DAY_OFFSET); // shift the current time so we can test whether the setvalue action was re-fired
        scenario.answer(22);

        assertThat(scenario.answerOf("/data/destination"), is(dateTimeAnswer(DATE_NOW)));

        scenario.answer(23);

        assertThat(scenario.answerOf("/data/destination"), is(dateTimeAnswer(DATE_NOW + DAY_OFFSET)));
    }

    @Test
    public void setvalue_isSerializedAndDeserialized() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("Nested setvalue action", html(
            head(
                title("Nested setvalue action"),
                model(
                    mainInstance(t("data id=\"nested-setvalue\"",
                        t("source"),
                        t("destination")
                    )),
                    bind("/data/source").type("int"),
                    bind("/data/destination").type("dateTime")
                )
            ),
            body(
                input("/data/source",
                    t("setvalue event=\"xforms-value-changed\" ref=\"/data/destination\" value=\"now()\""))
            )));

        scenario.serializeAndDeserializeForm();

        assertThat(scenario.answerOf("/data/destination"), is(nullValue()));

        scenario.next();
        scenario.answer(22);

        assertThat(scenario.answerOf("/data/destination"), is(dateTimeAnswer(DATE_NOW)));
    }

    @Test
    public void when_triggerNodeIsUpdatedWithinRepeat_targetNodeCalculation_isEvaluated() throws IOException {
        Scenario scenario = Scenario.init("Nested setvalue action with repeats", html(
            head(
                title("Nested setvalue action with repeats"),
                model(
                    mainInstance(t("data id=\"nested-setvalue-repeats\"",
                        repeat("repeat",
                            t("source"),
                            t("destination")
                        )
                    )),
                    bind("/data/repeat/source").type("int"),
                    bind("/data/repeat/destination").type("dateTime")
                )
            ),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/source",
                        t("setvalue event=\"xforms-value-changed\" ref=\"/data/repeat/destination\" value=\"now()\""))
                )
            )));

        final int REPEAT_COUNT = 5;

        for (int i = 0; i < REPEAT_COUNT; i++) {
            scenario.createNewRepeat("/data/repeat");
            assertThat(scenario.answerOf("/data/repeat[" + i + "]/destination"), is(nullValue()));
        }

        for (int i = 0; i < REPEAT_COUNT; i++) {
            DateTimeUtils.setCurrentMillisFixed(todayPlusDays(i));

            scenario.answer("/data/repeat[" + i + "]/source", 7);
        }

        for (int i = 0; i < REPEAT_COUNT; i++) {
            assertThat(scenario.answerOf("/data/repeat[" + i + "]/destination"), is(dateTimeAnswer(todayPlusDays(i))));
        }
    }

    /**
     * Read-only is a display-only concern so it should be possible to use an action to modify the value of a read-only
     * field.
     */
    @Test
    public void setvalue_setsValueOfReadOnlyField() throws IOException {
        Scenario scenario = Scenario.init("Setvalue readonly", html(
            head(
                title("Setvalue readonly"),
                model(
                    mainInstance(t("data id=\"setvalue-readonly\"",
                        t("readonly-field")
                    )),
                    bind("/data/readonly-field").readonly("1"),
                    t("setvalue event=\"odk-instance-first-load\" ref=\"/data/readonly-field\" value=\"now()\"")
                )
            ),
            body(
                input("/data/readonly-field")
            )));

        assertThat(scenario.answerOf("/data/readonly-field"), is(notNullValue()));
    }

    @Test
    public void setvalue_setsValueOfMultipleFields() throws IOException {
        Scenario scenario = Scenario.init("Setvalue multiple destinations", html(
            head(
                title("Setvalue multiple destinations"),
                model(
                    mainInstance(t("data id=\"setvalue-multiple\"",
                        t("source"),
                        t("destination1"),
                        t("destination2")
                    )),
                    bind("/data/destination1").type("int"),
                    bind("/data/destination2").type("int")
                )
            ),
            body(
                input("/data/source",
                    t("setvalue event=\"xforms-value-changed\" ref=\"/data/destination1\" value=\"7\""),
                    t("setvalue event=\"xforms-value-changed\" ref=\"/data/destination2\" value=\"11\""))
            )));

        scenario.answer("/data/source", "foo");
        assertThat(scenario.answerOf("/data/destination1"), is(intAnswer(7)));
        assertThat(scenario.answerOf("/data/destination2"), is(intAnswer(11)));
    }

    /**
     * This test demonstrates that read-only is just a UI concern and that calculates are still run on read-only fields.
     * Questions around this behavior came up while discussing setvalue. It's here for documentation purposes pending a
     * better place to put it.
     */
    @Test
    public void calculate_evaluatedOnReadonlyFieldWithUI() throws IOException {
        Scenario scenario = Scenario.init("Calculate readonly", html(
            head(
                title("Calculate readonly"),
                model(
                    mainInstance(t("data id=\"calculate-readonly\"",
                        t("readonly-calculate")
                    )),
                    bind("/data/readonly-calculate").readonly("1").calculate("7 * 2")
                )
            ),
            body(
                input("/data/readonly-calculate")
            )));

        assertThat(scenario.answerOf("/data/readonly-calculate"), is(intAnswer(14)));
    }

    private long todayPlusDays(int i) {
        return DATE_NOW + i * DAY_OFFSET;
    }
}
