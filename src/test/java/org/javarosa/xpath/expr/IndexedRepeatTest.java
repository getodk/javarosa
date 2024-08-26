package org.javarosa.xpath.expr;

import org.javarosa.test.Scenario;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.group;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.junit.Assert.fail;

public class IndexedRepeatTest {
    @Test
    public void firstArgNotChildOfRepeat_throwsException() throws Exception {
        try {
            Scenario.init("indexed-repeat", html(
                head(
                    title("indexed-repeat"),
                    model(
                        mainInstance(t("data id=\"indexed-repeat\"",
                            t("outside"),
                            t("repeat",
                                t("inside")),
                            t("calc")
                        )),
                        bind("/data/calc").calculate("indexed-repeat(/data/outside, /data/repeat, 1)")
                    )
                ),
                body(
                    input("/data/outside"),
                    repeat("/data/repeat",
                        input("/data/repeat/inside"))
                ))
            );

            fail("RuntimeException caused by XPathTypeMismatchException expected");
        } catch (RuntimeException e) {
            assertThat(e.getCause(), instanceOf(XPathTypeMismatchException.class));
        }
    }

    @Test
    public void getsIndexedValueInSingleRepeat() throws Exception {
        Scenario scenario = Scenario.init("indexed-repeat", html(
            head(
                title("indexed-repeat"),
                model(
                    mainInstance(t("data id=\"indexed-repeat\"",
                        t("index"),
                        t("outer_group", // included to clarify intended evaluation context for index references
                            t("repeat",
                                t("inside"))),
                        t("calc")
                    )),
                    bind("/data/calc").calculate("indexed-repeat(/data/outer_group/repeat/inside, /data/outer_group/repeat, ../index)")
                )
            ),
            body(
                input("/data/index"),
                group("/data/outer_group",
                    repeat("/data/outer_group/repeat",
                        input("/data/outer_group/repeat/inside")))
            ))
        );

        scenario.createNewRepeat("/data/outer_group[1]/repeat");
        scenario.answer("/data/outer_group[1]/repeat[1]/inside", "index1");

        scenario.createNewRepeat("/data/outer_group[1]/repeat");
        scenario.answer("/data/outer_group[1]/repeat[2]/inside", "index2");

        scenario.createNewRepeat("/data/outer_group[1]/repeat");
        scenario.answer("/data/outer_group[1]/repeat[3]/inside", "index3");

        scenario.answer("/data/index", "2");
        assertThat(scenario.answerOf("/data/calc"), is(stringAnswer("index2")));

        scenario.answer("/data/index", "1");
        assertThat(scenario.answerOf("/data/calc"), is(stringAnswer("index1")));
    }

    @Test
    public void getsIndexedValueUsingParallelRepeatPosition() throws Exception {
        Scenario scenario = Scenario.init("indexed-repeat", html(
            head(
                title("indexed-repeat"),
                model(
                    mainInstance(t("data id=\"indexed-repeat\"",
                        t("repeat1",
                            t("inside1")),

                        t("repeat2",
                            t("inside2"),
                            t("from_repeat1"))
                    )),
                    bind("/data/repeat2/from_repeat1").calculate("indexed-repeat(/data/repeat1/inside1, /data/repeat1, position(..))")
                )
            ),
            body(
                repeat("/data/repeat1",
                    input("/data/repeat1/inside1")),

                repeat("/data/repeat2",
                    input("/data/repeat2/inside2"))
            ))
        );

        scenario.createNewRepeat("/data/repeat1");
        scenario.createNewRepeat("/data/repeat2");
        scenario.answer("/data/repeat1[1]/inside1", "index1");

        scenario.createNewRepeat("/data/repeat1");
        scenario.createNewRepeat("/data/repeat2");
        scenario.answer("/data/repeat1[2]/inside1", "index2");

        scenario.createNewRepeat("/data/repeat1");
        scenario.createNewRepeat("/data/repeat2");
        scenario.answer("/data/repeat1[3]/inside1", "index3");

        assertThat(scenario.answerOf("/data/repeat2[1]/from_repeat1"), is(stringAnswer("index1")));
        assertThat(scenario.answerOf("/data/repeat2[2]/from_repeat1"), is(stringAnswer("index2")));
    }
}
