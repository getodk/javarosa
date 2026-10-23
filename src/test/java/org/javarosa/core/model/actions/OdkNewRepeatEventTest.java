package org.javarosa.core.model.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.ResourcePathHelper.r;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.group;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.setvalue;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import java.io.IOException;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

/**
 * Specification: https://getodk.github.io/xforms-spec/#the-odk-new-repeat-event.
 */
public class OdkNewRepeatEventTest {
    @Test
    public void setValueOnRepeatInsertInBody_setsValueInRepeat() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        assertThat(scenario.countRepeatInstancesOf("/data/my-repeat"), is(0));
        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.countRepeatInstancesOf("/data/my-repeat"), is(1));
        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-position").getDisplayText(), is("1"));
    }

    @Test
    public void addingRepeat_doesNotChangeValueSetForPreviousRepeat() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-position").getDisplayText(), is("1"));

        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.answerOf("/data/my-repeat[2]/defaults-to-position").getDisplayText(), is("2"));

        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-position").getDisplayText(), is("1"));
    }

    @Test
    public void setValueOnRepeatInBody_usesCurrentContextForRelativeReferences() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.answer("/data/my-toplevel-value", "12");

        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-toplevel").getDisplayText(), is("14"));
    }

    @Test
    public void setValueOnRepeatWithCount_setsValueForEachRepeat() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.answer("/data/repeat-count", 4);

        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }

        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(4));

        assertThat(scenario.answerOf("/data/my-jr-count-repeat[1]/defaults-to-position-again").getDisplayText(), is("1"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[2]/defaults-to-position-again").getDisplayText(), is("2"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[3]/defaults-to-position-again").getDisplayText(), is("3"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[4]/defaults-to-position-again").getDisplayText(), is("4"));

        // Adding repeats should trigger odk-new-repeat for those new nodes
        scenario.answer("/data/repeat-count", 6);

        scenario.jumpToBeginningOfForm();
        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }
        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(6));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[6]/defaults-to-position-again").getDisplayText(), is("6"));

    }

    @Test
    public void setOtherThanIntegerValueOnRepeatWithCount_convertsValueToInteger() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        // String
        scenario.answer("/data/repeat-count", "1");
        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }
        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(1));

        // Decimal
        scenario.jumpToBeginningOfForm();
        scenario.answer("/data/repeat-count", 2.5);
        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }
        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(2));

        // Long
        scenario.jumpToBeginningOfForm();
        scenario.answer("/data/repeat-count", 3L);
        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }
        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(3));
    }

    @Test
    public void repeatInFormDefInstance_neverFiresNewRepeatEvent() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        assertThat(scenario.answerOf("/data/my-repeat-without-template[1]/my-value"), is(nullValue()));
        assertThat(scenario.answerOf("/data/my-repeat-without-template[2]/my-value"), is(nullValue()));

        scenario.createNewRepeat("/data/my-repeat-without-template");
        assertThat(scenario.answerOf("/data/my-repeat-without-template[3]/my-value").getDisplayText(), is("2"));
    }

    @Test
    public void newRepeatInstance_doesNotTriggerActionOnUnrelatedRepeat() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Parallel repeats"),
                model(
                    mainInstance(t("data id=\"parallel-repeats\"",
                        t("repeat1",
                            t("q1")),

                        t("repeat2",
                            t("q1")
                        )
                    ))
                )
            ),
            body(
                repeat("/data/repeat1",
                    setvalue("odk-new-repeat", "/data/repeat1/q1", "concat('foo','bar')"),
                    input("/data/repeat1/q1")
                ),
                repeat("/data/repeat2",
                    setvalue("odk-new-repeat", "/data/repeat2/q1", "concat('bar','baz')"),
                    input("/data/repeat2/q1")
                ))));

        scenario.createNewRepeat("/data/repeat1");
        scenario.createNewRepeat("/data/repeat1");

        scenario.createNewRepeat("/data/repeat2");
        scenario.createNewRepeat("/data/repeat2");

        assertThat(scenario.answerOf("/data/repeat1[2]/q1").getDisplayText(), is("foobar"));
        assertThat(scenario.answerOf("/data/repeat1[3]/q1").getDisplayText(), is("foobar"));

        assertThat(scenario.answerOf("/data/repeat2[2]/q1").getDisplayText(), is("barbaz"));
        assertThat(scenario.answerOf("/data/repeat2[3]/q1").getDisplayText(), is("barbaz"));
    }

    @Test
    public void newRepeatInstance_canUsePreviousInstanceAsDefault() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Default from prior instance"),
                model(
                    mainInstance(t("data id=\"default-from-prior-instance\"",
                        t("repeat",
                            t("q"))
                    )),
                    bind("/data/repeat/q").type("int")
                )
            ),
            body(
                repeat("/data/repeat",
                    setvalue("odk-new-repeat", "/data/repeat/q", "/data/repeat[position()=position(current()/..)-1]/q"),
                    input("/data/repeat/q")
                ))));
        scenario.next();
        scenario.next();
        scenario.answer(7);
        assertThat(scenario.answerOf("/data/repeat[1]/q"), is(intAnswer(7)));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        assertThat(scenario.answerOf("/data/repeat[2]/q"), is(intAnswer(7)));
        scenario.answer(8); // override the default

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        assertThat(scenario.answerOf("/data/repeat[1]/q"), is(intAnswer(7)));
        assertThat(scenario.answerOf("/data/repeat[2]/q"), is(intAnswer(8)));
        assertThat(scenario.answerOf("/data/repeat[3]/q"), is(intAnswer(8)));
    }

    // Not part of ODK XForms so throws parse exception.
    @Test(expected = XFormParseException.class)
    public void setValueOnRepeatInsertInModel_notAllowed() throws XFormParser.ParseException {
        Scenario.init(r("event-odk-new-repeat-model.xml"));
    }

    //region nested repeats
    @Test
    public void creatingOuterRepeat_triggersSetValueExactlyOnce_forNestedConcreteInstances() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Nested repeat actions execute once"),
                model(
                    mainInstance(t("data id=\"nested-repeat-actions-once\"",
                        t("outer jr:template=\"\"",
                            t("inner jr:template=\"\"", t("value")),
                            t("inner", t("value"))
                        ),
                        t("outer",
                            t("inner", t("value"))
                        )
                    )),
                    bind("/data/outer/inner/value").type("int"),
                    setvalue("odk-instance-first-load", "/data/outer/inner/value", "10")
                )
            ),
            body(
                group("/data/outer",
                    repeat("/data/outer",
                        group("/data/outer/inner",
                            repeat("/data/outer/inner",
                                setvalue(
                                    "odk-new-repeat",
                                    "/data/outer/inner/value",
                                    "coalesce(., 0) + 1" // A duplicate event would produce 2
                                ),
                                input("/data/outer/inner/value")
                            )
                        )
                    )
                )
            )
        ));

        // Initial instances receive odk-instance-first-load, not odk-new-repeat
        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(1));
        assertThat(scenario.answerOf("/data/outer[1]/inner[1]/value"), is(intAnswer(10)));

        // Explicitly creating an inner repeat fires odk-new-repeat once
        scenario.createNewRepeat("/data/outer[1]/inner");
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(2));
        assertThat(scenario.answerOf("/data/outer[1]/inner[2]/value"), is(intAnswer(1)));

        // Creating an outer repeat implicitly creates and initializes its inner concrete instance
        scenario.createNewRepeat("/data/outer");
        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/inner"), is(1));
        assertThat(scenario.answerOf("/data/outer[2]/inner[1]/value"), is(intAnswer(1)));

        // Creating more outer repeats also implicitly creates and initializes inner concrete instances
        scenario.createNewRepeat("/data/outer");
        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(3));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[3]/inner"), is(1));
        assertThat(scenario.answerOf("/data/outer[3]/inner[1]/value"), is(intAnswer(1)));

        // Creating new repeats does not re-trigger actions on existing instances
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(2));
        assertThat(scenario.answerOf("/data/outer[1]/inner[1]/value"), is(intAnswer(10)));
        assertThat(scenario.answerOf("/data/outer[1]/inner[2]/value"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/outer[2]/inner[1]/value"), is(intAnswer(1)));
    }

    @Test
    public void creatingOuterRepeat_triggersSetValue_forEveryNestedConcreteInstance() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Multiple nested concrete instances"),
                model(
                    mainInstance(t("data id=\"multiple-nested-starters\"",
                        t("outer jr:template=\"\"",
                            t("inner jr:template=\"\"", t("value")),
                            t("inner", t("value")),
                            t("inner", t("value"))
                        ),
                        t("outer",
                            t("inner", t("value"))
                        )
                    )),
                    bind("/data/outer/inner/value").type("int")
                )
            ),
            body(
                group("/data/outer",
                    repeat("/data/outer",
                        group("/data/outer/inner",
                            repeat("/data/outer/inner",
                                setvalue(
                                    "odk-new-repeat",
                                    "/data/outer/inner/value",
                                    "coalesce(., 0) + 1"
                                ),
                                input("/data/outer/inner/value")
                            )
                        )
                    )
                )
            )
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/inner"), is(2)); // template has 2 concrete instances
        assertThat(scenario.answerOf("/data/outer[2]/inner[1]/value"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/outer[2]/inner[2]/value"), is(intAnswer(1)));

        // Concrete instances at load are unchanged
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));
        assertThat(scenario.answerOf("/data/outer[1]/inner[1]/value"), is(nullValue()));
    }

    @Test
    public void creatingOuterRepeat_triggersNewRepeatThroughInterveningGroup() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Nested repeat through group"),
                model(
                    mainInstance(t("data id=\"nested-repeat-through-group\"",
                        t("outer jr:template=\"\"",
                            t("details",
                                t("inner jr:template=\"\"", t("value")),
                                t("inner", t("value"))
                            )
                        ),
                        t("outer",
                            t("details",
                                t("inner", t("value"))
                            )
                        )
                    )),
                    bind("/data/outer/details/inner/value").type("int")
                )
            ),
            body(
                group("/data/outer",
                    repeat("/data/outer",
                        group("/data/outer/details",
                            group("/data/outer/details/inner",
                                repeat("/data/outer/details/inner",
                                    setvalue(
                                        "odk-new-repeat",
                                        "/data/outer/details/inner/value",
                                        "coalesce(., 0) + 1"
                                    ),
                                    input("/data/outer/details/inner/value")
                                )
                            )
                        )
                    )
                )
            )
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/details/inner"), is(1));
        assertThat(scenario.answerOf("/data/outer[2]/details/inner[1]/value"), is(intAnswer(1)));
    }

    @Test
    public void creatingOuterRepeat_triggersNewRepeat_forSiblingNestedRepeats() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Sibling nested repeat actions"),
                model(
                    mainInstance(t("data id=\"sibling-nested-repeat-actions\"",
                        t("outer jr:template=\"\"",
                            t("r1 jr:template=\"\"", t("value")),
                            t("r1", t("value")),
                            t("r2 jr:template=\"\"", t("value")),
                            t("r2", t("value"))
                        ),
                        t("outer",
                            t("r1", t("value")),
                            t("r2", t("value"))
                        )
                    )),
                    bind("/data/outer/r1/value").type("int"),
                    bind("/data/outer/r2/value").type("int")
                )
            ),
            body(
                group("/data/outer",
                    repeat("/data/outer",
                        group("/data/outer/r1",
                            repeat("/data/outer/r1",
                                setvalue(
                                    "odk-new-repeat",
                                    "/data/outer/r1/value",
                                    "coalesce(., 0) + 1"
                                ),
                                input("/data/outer/r1/value")
                            )
                        ),
                        group("/data/outer/r2",
                            repeat("/data/outer/r2",
                                setvalue(
                                    "odk-new-repeat",
                                    "/data/outer/r2/value",
                                    "coalesce(., 0) + 2"
                                ),
                                input("/data/outer/r2/value")
                            )
                        )
                    )
                )
            )
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/r1"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/r2"), is(1));

        assertThat(scenario.answerOf("/data/outer[2]/r1[1]/value"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/outer[2]/r2[1]/value"), is(intAnswer(2)));

        // Existing instances should not receive odk-new-repeat.
        assertThat(scenario.answerOf("/data/outer[1]/r1[1]/value"), is(nullValue()));
        assertThat(scenario.answerOf("/data/outer[1]/r2[1]/value"), is(nullValue()));
    }

    @Test
    public void creatingOuterRepeat_triggersNewRepeatAndCalculations_atEveryNestedLevel() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Three-level nested repeat actions and calculations"),
                model(
                    mainInstance(t("data id=\"three-level-nested-repeat-actions\"",
                        t("outer jr:template=\"\"",
                            t("middle jr:template=\"\"",
                                t("value"),
                                t("inner jr:template=\"\"", t("value"), t("calculated")),
                                t("inner", t("value"), t("calculated"))
                            ),
                            t("middle",
                                t("value"),
                                t("inner", t("value"), t("calculated"))
                            )
                        ),
                        t("outer",
                            t("middle",
                                t("value"),
                                t("inner", t("value"), t("calculated"))
                            )
                        )
                    )),
                    bind("/data/outer/middle/value").type("int"),
                    bind("/data/outer/middle/inner/value").type("int"),
                    bind("/data/outer/middle/inner/calculated").type("int").calculate("../value * 2")
                )
            ),
            body(
                group("/data/outer",
                    repeat("/data/outer",
                        group("/data/outer/middle",
                            repeat("/data/outer/middle",
                                setvalue("odk-new-repeat", "/data/outer/middle/value", "coalesce(., 0) + 1"),
                                input("/data/outer/middle/value"),
                                group("/data/outer/middle/inner",
                                    repeat("/data/outer/middle/inner",
                                        setvalue("odk-new-repeat", "/data/outer/middle/inner/value", "coalesce(., 0) + 1"),
                                        input("/data/outer/middle/inner/value")
                )))))))
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle[1]/inner"), is(1));

        // Both nested levels receive odk-new-repeat exactly once.
        assertThat(scenario.answerOf("/data/outer[2]/middle[1]/value"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/outer[2]/middle[1]/inner[1]/value"), is(intAnswer(1)));

        // The innermost calculation is evaluated for the newly copied instance.
        assertThat(scenario.answerOf("/data/outer[2]/middle[1]/inner[1]/calculated"), is(intAnswer(2)));

        // Existing instances remain unchanged.
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/value"), is(nullValue()));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/inner[1]/value"), is(nullValue()));
    }

    @Test
    public void creatingOuterRepeat_usesConcreteNestedInstanceDefaults_notTemplateDefaults() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Nested repeat template defaults"),
                model(
                    mainInstance(t("data id=\"nested-template-default\"",
                        t("outer jr:template=\"\"",
                            t("inner jr:template=\"\"", t("value", "100")),
                            t("inner", t("value", "10"))
                        ),
                        t("outer",
                            t("inner", t("value", "10"))
                        )
                    )),
                    bind("/data/outer/inner/value").type("int")
                )
            ),
            body(
                group("/data/outer",
                    repeat("/data/outer",
                        group("/data/outer/inner",
                            repeat("/data/outer/inner",
                                setvalue(
                                    "odk-new-repeat",
                                    "/data/outer/inner/value",
                                    ". + 1"
                                ),
                                input("/data/outer/inner/value")
                            )
                        )
                    )
                )
            )
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.answerOf("/data/outer[2]/inner[1]/value"), is(intAnswer(11)));
        assertThat(scenario.answerOf("/data/outer[1]/inner[1]/value"), is(intAnswer(10)));

        scenario.createNewRepeat("/data/outer[2]/inner");

        assertThat(scenario.answerOf("/data/outer[2]/inner[2]/value"), is(intAnswer(101)));
    }
    //endregion nested repeats
}
