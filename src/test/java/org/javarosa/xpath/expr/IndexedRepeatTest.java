package org.javarosa.xpath.expr;

import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.junit.Test;

import java.io.IOException;

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

    @Test
    public void handlesTopLevelRepeats() throws IOException, XFormParser.ParseException {
        Scenario scenario = buildNestedRepeatForm();
        assertThat(scenario.answerOf("/data/r2-d1[1]/from-r1-d1"), is(stringAnswer("[1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/from-r1-d1"), is(stringAnswer("[2]")));
    }

    @Test
    public void handlesRepeatsTwoDeep() throws IOException, XFormParser.ParseException {
        Scenario scenario = buildNestedRepeatForm();

        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[1]/from-r1-d2-a"), is(stringAnswer("[1][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[2]/from-r1-d2-a"), is(stringAnswer("[1][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[1]/from-r1-d2-a"), is(stringAnswer("[2][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[2]/from-r1-d2-a"), is(stringAnswer("[2][2]")));

        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[1]/from-r1-d2-b"), is(stringAnswer("[1][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[2]/from-r1-d2-b"), is(stringAnswer("[1][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[1]/from-r1-d2-b"), is(stringAnswer("[2][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[2]/from-r1-d2-b"), is(stringAnswer("[2][2]")));
    }

    @Test
    public void handlesRepeatsThreeDeep() throws IOException, XFormParser.ParseException {
        Scenario scenario = buildNestedRepeatForm();

        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[1]/r2-d3[1]/from-r1-d3-a"), is(stringAnswer("[1][1][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[1]/r2-d3[2]/from-r1-d3-a"), is(stringAnswer("[1][1][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[2]/r2-d3[1]/from-r1-d3-a"), is(stringAnswer("[1][2][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[2]/r2-d3[2]/from-r1-d3-a"), is(stringAnswer("[1][2][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[1]/r2-d3[1]/from-r1-d3-a"), is(stringAnswer("[2][1][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[1]/r2-d3[2]/from-r1-d3-a"), is(stringAnswer("[2][1][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[2]/r2-d3[1]/from-r1-d3-a"), is(stringAnswer("[2][2][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[2]/r2-d3[2]/from-r1-d3-a"), is(stringAnswer("[2][2][2]")));

        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[1]/r2-d3[1]/from-r1-d3-b"), is(stringAnswer("[1][1][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[1]/r2-d3[2]/from-r1-d3-b"), is(stringAnswer("[1][1][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[2]/r2-d3[1]/from-r1-d3-b"), is(stringAnswer("[1][2][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[1]/r2-d2[2]/r2-d3[2]/from-r1-d3-b"), is(stringAnswer("[1][2][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[1]/r2-d3[1]/from-r1-d3-b"), is(stringAnswer("[2][1][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[1]/r2-d3[2]/from-r1-d3-b"), is(stringAnswer("[2][1][2]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[2]/r2-d3[1]/from-r1-d3-b"), is(stringAnswer("[2][2][1]")));
        assertThat(scenario.answerOf("/data/r2-d1[2]/r2-d2[2]/r2-d3[2]/from-r1-d3-b"), is(stringAnswer("[2][2][2]")));
    }

    public static Scenario buildNestedRepeatForm() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("indexed-repeat", html(
            head(
                title("indexed-repeat"),
                model(
                    mainInstance(t("data id=\"indexed-repeat\"",
                        t("r1-d1 jr:template=\"\"",
                            t("inside-r1-d1"),
                            t("r1-d2 jr:template=\"\"",
                                t("inside-r1-d2"),
                                t("r1-d3 jr:template=\"\"",
                                    t("inside-r1-d3")))),
                        t("r2-d1 jr:template=\"\"",
                            t("inside-r2-d1"),
                            t("from-r1-d1"),
                            t("r2-d2 jr:template=\"\"",
                                t("inside-r2-d2"),
                                t("from-r1-d2-a"),
                                t("from-r1-d2-b"),
                                t("r2-d3 jr:template=\"\"",
                                    t("inside-r2-d3"),
                                    t("from-r1-d3-a"),
                                    t("from-r1-d3-b"))))
                    )),
                    bind("/data/r1-d1/inside-r1-d1")
                        .calculate("concat('[', position(..), ']')"),
                    bind("/data/r1-d1/r1-d2/inside-r1-d2")
                        .calculate("concat('[', position(../..), ']', '[', position(..), ']')"),
                    bind("/data/r1-d1/r1-d2/r1-d3/inside-r1-d3")
                        .calculate("concat('[', position(../../..), ']', '[', position(../..), ']', '[', position(..), ']')"),
                    bind("/data/r2-d1/from-r1-d1")
                        .calculate("indexed-repeat(/data/r1-d1/inside-r1-d1, /data/r1-d1, position(..))"),
                    bind("/data/r2-d1/r2-d2/from-r1-d2-a")
                        .calculate("indexed-repeat(/data/r1-d1/r1-d2/inside-r1-d2, /data/r1-d1, position(../..), /data/r1-d1/r1-d2, position(..))"),
                    bind("/data/r2-d1/r2-d2/from-r1-d2-b")
                        // Same as from-r1-d2-a with the repeatN/indexN pairs swapped
                        .calculate("indexed-repeat(/data/r1-d1/r1-d2/inside-r1-d2, /data/r1-d1/r1-d2, position(..), /data/r1-d1, position(../..))"),
                    bind("/data/r2-d1/r2-d2/r2-d3/from-r1-d3-a")
                        .calculate("indexed-repeat(/data/r1-d1/r1-d2/r1-d3/inside-r1-d3, /data/r1-d1, position(../../..), /data/r1-d1/r1-d2, position(../..), /data/r1-d1/r1-d2/r1-d3, position(..))"),
                    bind("/data/r2-d1/r2-d2/r2-d3/from-r1-d3-b")
                        // Same as from-r1-d3-a with the repeatN/indexN pairs reordered
                        .calculate("indexed-repeat(/data/r1-d1/r1-d2/r1-d3/inside-r1-d3, /data/r1-d1/r1-d2, position(../..), /data/r1-d1, position(../../..), /data/r1-d1/r1-d2/r1-d3, position(..))")
                )
            ),
            body(
                repeat("/data/r1-d1",
                    input("/data/r1-d1/inside-r1-d1"),
                    repeat("/data/r1-d1/r1-d2",
                        input("/data/r1-d1/r1-d2/inside-r1-d2"),
                        repeat("/data/r1-d1/r1-d2/r1-d3",
                            input("/data/r1-d1/r1-d2/r1-d3/inside-r1-d3")))),
                repeat("/data/r2-d1",
                    input("/data/r2-d1/inside-r2-d1"),
                    repeat("/data/r2-d1/r2-d2",
                        input("/data/r2-d1/r2-d2/inside-r2-d2"),
                        repeat("/data/r2-d1/r2-d2/r2-d3",
                            input("/data/r2-d1/r2-d2/r2-d3/inside-r2-d3"))))
            )));

        scenario.createNewRepeat("/data/r1-d1");
        scenario.createNewRepeat("/data/r1-d1");

        scenario.createNewRepeat("/data/r1-d1[1]/r1-d2");
        scenario.createNewRepeat("/data/r1-d1[1]/r1-d2");
        scenario.createNewRepeat("/data/r1-d1[2]/r1-d2");
        scenario.createNewRepeat("/data/r1-d1[2]/r1-d2");

        scenario.createNewRepeat("/data/r1-d1[1]/r1-d2[1]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[1]/r1-d2[1]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[1]/r1-d2[2]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[1]/r1-d2[2]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[2]/r1-d2[1]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[2]/r1-d2[1]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[2]/r1-d2[2]/r1-d3");
        scenario.createNewRepeat("/data/r1-d1[2]/r1-d2[2]/r1-d3");

        scenario.createNewRepeat("/data/r2-d1");
        scenario.createNewRepeat("/data/r2-d1");

        scenario.createNewRepeat("/data/r2-d1[1]/r2-d2");
        scenario.createNewRepeat("/data/r2-d1[1]/r2-d2");
        scenario.createNewRepeat("/data/r2-d1[2]/r2-d2");
        scenario.createNewRepeat("/data/r2-d1[2]/r2-d2");

        scenario.createNewRepeat("/data/r2-d1[1]/r2-d2[1]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[1]/r2-d2[1]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[1]/r2-d2[2]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[1]/r2-d2[2]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[2]/r2-d2[1]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[2]/r2-d2[1]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[2]/r2-d2[2]/r2-d3");
        scenario.createNewRepeat("/data/r2-d1[2]/r2-d2[2]/r2-d3");

        return scenario;
    }
}
