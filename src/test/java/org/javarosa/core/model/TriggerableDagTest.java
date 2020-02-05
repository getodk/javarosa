package org.javarosa.core.model;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.group;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.BindBuilderXFormsElement;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.debug.Event;
import org.javarosa.xform.parse.XFormParseException;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerableDagTest {
    private static Logger logger = LoggerFactory.getLogger(TriggerableDagTest.class);

    private List<Event> dagEvents = new ArrayList<>();

    @Before
    public void setUp() {
        dagEvents.clear();
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnPrecedingRepeatGroupSiblings() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("house jr:template=\"\"", t("number"))
                    )),
                    bind("/data/house/number").type("int").calculate("position(..)")
                )
            ),
            body(group("/data/house", repeat("/data/house", input("number"))))
        )).onDagEvent(dagEvents::add);
        range(0, 5).forEach(__ -> {
            scenario.next();
            scenario.createNewRepeat();
            scenario.next();
        });
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(intAnswer(5)));

        // Start recording DAG events now
        dagEvents.clear();

        scenario.removeRepeat("/data/house[1]");

        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(nullValue()));
        assertDagEvents(dagEvents,
            "Processing 'Recalculate' for number [2_1] (2.0)",
            "Processing 'Deleted: house [2]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: number [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [3_1] (3.0)",
            "Processing 'Deleted: house [3]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [4_1] (4.0)",
            "Processing 'Deleted: house [4]: 1 triggerables were fired.' for "
        );
    }

    @Test
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnTheParentPosition() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("house jr:template=\"\"",
                            t("number"),
                            t("name"),
                            t("name_and_number")
                        )
                    )),
                    bind("/data/house/number").type("int").calculate("position(..)"),
                    bind("/data/house/name").type("string").required(),
                    bind("/data/house/name_and_number").type("string").calculate("concat(/data/house/name, /data/house/number)")
                )
            ),
            body(group("/data/house", repeat("/data/house", input("/data/house/name"))))
        )).onDagEvent(dagEvents::add);
        range(0, 5).forEach(n -> {
            scenario.next();
            scenario.createNewRepeat();
            scenario.next();
            scenario.answer((char) (65 + n));
        });
        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("A1")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("B2")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("C3")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("D4")));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(stringAnswer("E5")));

        // Start recording DAG events now
        dagEvents.clear();

        scenario.removeRepeat("/data/house[1]");

        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("A1")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("C2")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("D3")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("E4")));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(nullValue()));
        assertDagEvents(dagEvents,
            "Processing 'Recalculate' for number [2_1] (2.0)",
            "Processing 'Recalculate' for name_and_number [2_1] (C2)",
            "Processing 'Deleted: house [2]: 2 triggerables were fired.' for ",
            "Processing 'Deleted: number [2_1]: 0 triggerables were fired.' for ",
            "Processing 'Deleted: name [2_1]: 0 triggerables were fired.' for ",
            "Processing 'Deleted: name_and_number [2_1]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [3_1] (3.0)",
            "Processing 'Recalculate' for name_and_number [3_1] (D3)",
            "Processing 'Deleted: house [3]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [4_1] (4.0)",
            "Processing 'Recalculate' for name_and_number [4_1] (E4)",
            "Processing 'Deleted: house [4]: 2 triggerables were fired.' for "
        );
    }

    @Test
    public void deleteSecondRepeatGroup_doesNotEvaluateTriggerables_notDependentOnTheParentPosition() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("house jr:template=\"\"",
                            t("number"),
                            t("name"),
                            t("name_and_number")
                        )
                    )),
                    bind("/data/house/number").type("int").calculate("position(..)"),
                    bind("/data/house/name").type("string").required(),
                    bind("/data/house/name_and_number").type("string").calculate("concat(/data/house/name, 'X')")
                )
            ),
            body(group("/data/house", repeat("/data/house", input("/data/house/name"))))
        )).onDagEvent(dagEvents::add);
        range(0, 5).forEach(n -> {
            scenario.next();
            scenario.createNewRepeat();
            scenario.next();
            scenario.answer((char) (65 + n));
        });
        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("AX")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("BX")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("CX")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("DX")));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(stringAnswer("EX")));

        // Start recording DAG events now
        dagEvents.clear();

        scenario.removeRepeat("/data/house[1]");

        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("AX")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("CX")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("DX")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("EX")));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(nullValue()));
        assertDagEvents(dagEvents,
            "Processing 'Recalculate' for number [2_1] (2.0)",
            "Processing 'Deleted: house [2]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: number [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for name_and_number [2_1] (CX)",
            "Processing 'Deleted: name [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: name_and_number [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [3_1] (3.0)",
            "Processing 'Deleted: house [3]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [4_1] (4.0)",
            "Processing 'Deleted: house [4]: 1 triggerables were fired.' for "
        );
    }

    @Test
    public void deleteThirdRepeatGroup_evaluatesTriggerables_dependentOnTheRepeatGroupsNumber() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("house jr:template=\"\"", t("number")),
                        t("summary")
                    )),
                    bind("/data/house/number").type("int").calculate("position(..)"),
                    bind("/data/summary").type("int").calculate("sum(/data/house/number)")
                )
            ),
            body(group("/data/house", repeat("/data/house", input("number"))))
        )).onDagEvent(dagEvents::add);
        range(0, 10).forEach(n -> {
            scenario.next();
            scenario.createNewRepeat();
            scenario.next();
        });
        assertThat(scenario.answerOf("/data/summary"), is(intAnswer(55)));

        // Start recording DAG events now
        dagEvents.clear();

        scenario.removeRepeat("/data/house[2]");

        assertThat(scenario.answerOf("/data/summary"), is(intAnswer(45)));
        assertDagEvents(dagEvents,
            "Processing 'Recalculate' for number [3_1] (3.0)",
            "Processing 'Recalculate' for summary [1] (51.0)",
            "Processing 'Deleted: house [3]: 2 triggerables were fired.' for ",
            "Processing 'Deleted: number [3_1]: 0 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [4_1] (4.0)",
            "Processing 'Recalculate' for summary [1] (50.0)",
            "Processing 'Deleted: house [4]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [5_1] (5.0)",
            "Processing 'Recalculate' for summary [1] (49.0)",
            "Processing 'Deleted: house [5]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [6_1] (6.0)",
            "Processing 'Recalculate' for summary [1] (48.0)",
            "Processing 'Deleted: house [6]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [7_1] (7.0)",
            "Processing 'Recalculate' for summary [1] (47.0)",
            "Processing 'Deleted: house [7]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [8_1] (8.0)",
            "Processing 'Recalculate' for summary [1] (46.0)",
            "Processing 'Deleted: house [8]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for number [9_1] (9.0)",
            "Processing 'Recalculate' for summary [1] (45.0)",
            "Processing 'Deleted: house [9]: 2 triggerables were fired.' for "
        );
    }

    /**
     * Indirectly means that the calculation - `concat(/data/house/name)` - does
     * not take the
     * `/data/house` nodeset (the repeat group) as an argument
     * but since it takes one of its children (`name` children),
     * the calculation must re-evaluated once after a repeat group deletion
     * because one of the children
     * has been deleted along with its parent (the repeat group instance).
     */
    @Test
    public void deleteThirdRepeatGroup_evaluatesTriggerables_indirectlyDependentOnTheRepeatGroupsNumber() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("house jr:template=\"\"", t("name")),
                        t("summary")
                    )),
                    bind("/data/house/name").type("string").required(),
                    bind("/data/summary").type("string").calculate("concat(/data/house/name)")
                )
            ),
            body(group("/data/house", repeat("/data/house", input("/data/house/name"))))
        )).onDagEvent(dagEvents::add);
        range(0, 5).forEach(n -> {
            scenario.next();
            scenario.createNewRepeat();
            scenario.next();
            scenario.answer((char) (65 + n));
        });
        assertThat(scenario.answerOf("/data/summary"), is(stringAnswer("ABCDE")));

        // Start recording DAG events now
        dagEvents.clear();

        scenario.removeRepeat("/data/house[2]");

        assertThat(scenario.answerOf("/data/summary"), is(stringAnswer("ABDE")));
        assertDagEvents(dagEvents,
            "Processing 'Deleted: house [3]: 0 triggerables were fired.' for ",
            "Processing 'Recalculate' for summary [1] (ABDE)",
            "Processing 'Deleted: name [3_1]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: house [4]: 0 triggerables were fired.' for "
        );

    }

    /**
     * This test was inspired by the issue reported at https://code.google.com/archive/p/opendatakit/issues/888
     * <p>
     * We want to focus on the relationship between relevancy and other
     * calculations because relevancy can be
     * defined for fields **and groups**, which is a special case of expression
     * evaluation in our DAG.
     */
    @Test
    public void verify_relation_between_calculate_expressions_and_relevancy_conditions() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(
                        t("data id=\"some-form\"",
                            t("number1"),
                            t("continue"),
                            t("group", t("number1_x2"), t("number1_x2_x2"), t("number2"))
                        )
                    ),
                    bind("/data/number1").type("int").constraint(". > 0").required(),
                    bind("/data/continue").type("string").required(),
                    bind("/data/group").relevant("/data/continue = '1'"),
                    bind("/data/group/number1_x2").type("int").calculate("/data/number1 * 2"),
                    bind("/data/group/number1_x2_x2").type("int").calculate("/data/group/number1_x2 * 2"),
                    bind("/data/group/number2").type("int").relevant("/data/group/number1_x2 > 0").required()
                )
            ),
            body(
                input("/data/number1"),
                select1("/data/continue",
                    item(1, "Yes"),
                    item(0, "No")
                ),
                group("/data/group",
                    input("/data/group/number2")
                )
            )
        ));
        scenario.next();
        scenario.answer(2);
        // Notice how the calculate at number1_x2 gets evaluated even though it's in a non-relevant group
        // and number1_x2_x2 doesn't get evaluated. The difference could be that the second field depends
        // on a field that is inside a non-relevant group.
        assertThat(scenario.answerOf("/data/group/number1_x2"), is(intAnswer(4)));
        // TODO figure out why group/number1_x2_x2 isn't evaluated once number1 gets a value. Should all expressions be evaluated regardless of relevance? In any case, describe and document which expressions are evaluated when
        assertThat(scenario.answerOf("/data/group/number1_x2_x2"), is(nullValue()));
        scenario.next();
        scenario.answer("1"); // Label: "yes"
        assertThat(scenario.answerOf("/data/group/number1_x2"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/group/number1_x2_x2"), is(intAnswer(8)));
    }

    /**
     * Ignored because the assertions about non-null next-numbers will fail
     * because our DAG
     * doesn't evaluate calculations in repeat instances that are previous
     * siblings to the
     * one that has changed.
     * <p>
     * The expeculation is that, originally, only forward references might have
     * been considered
     * to speed up repeat group intance deletion because it was assumed that
     * back references
     * were a marginal use case.
     * <p>
     * This test explores how the issue affects to value changes too.
     */
    @Test
    @Ignore
    public void calculate_expressions_should_be_evaluated_on_previous_repeat_siblings() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group jr:template=\"\"",
                            t("prev-number"),
                            t("number"),
                            t("next-number")
                        )
                    )),
                    bind("/data/group/prev-number").type("int").calculate("/data/group[position() = (position(current()/..) - 1)]/number"),
                    bind("/data/group/number").type("int").required(),
                    bind("/data/group/next-number").type("int").calculate("/data/group[position() = (position(current()/..) + 1)]/number")
                )
            ),
            body(group("/data/group", repeat("/data/group", input("/data/group/number"))))
        ));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        scenario.answer(11);

        assertThat(scenario.answerOf("/data/group[0]/prev-number"), is(nullValue()));

        assertThat(scenario.answerOf("/data/group[0]/number"), is(intAnswer(11)));

        assertThat(scenario.answerOf("/data/group[0]/next-number"), is(nullValue()));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        scenario.answer(22);

        assertThat(scenario.answerOf("/data/group[0]/prev-number"), is(nullValue()));
        assertThat(scenario.answerOf("/data/group[1]/prev-number"), is(intAnswer(11)));

        assertThat(scenario.answerOf("/data/group[0]/number"), is(intAnswer(11)));
        assertThat(scenario.answerOf("/data/group[1]/number"), is(intAnswer(22)));

        // This assertion is the one that fails with the current implementation
        assertThat(scenario.answerOf("/data/group[0]/next-number"), is(intAnswer(22)));
        assertThat(scenario.answerOf("/data/group[1]/next-number"), is(nullValue()));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        scenario.answer(33);

        assertThat(scenario.answerOf("/data/group[0]/prev-number"), is(nullValue()));
        assertThat(scenario.answerOf("/data/group[1]/prev-number"), is(intAnswer(11)));
        assertThat(scenario.answerOf("/data/group[2]/prev-number"), is(intAnswer(22)));

        assertThat(scenario.answerOf("/data/group[0]/number"), is(intAnswer(11)));
        assertThat(scenario.answerOf("/data/group[1]/number"), is(intAnswer(22)));
        assertThat(scenario.answerOf("/data/group[2]/number"), is(intAnswer(33)));

        // The following couple of assertions are the ones that fail with the current implementation
        assertThat(scenario.answerOf("/data/group[0]/next-number"), is(intAnswer(22)));
        assertThat(scenario.answerOf("/data/group[1]/next-number"), is(intAnswer(33)));
        assertThat(scenario.answerOf("/data/group[2]/next-number"), is(nullValue()));
    }

    /**
     * Ignored because the count() function inside the predicate of the result_2
     * calculate
     * expression isn't evaled correctly, as opposed to the predicate of the
     * result_1 calculate
     * that uses an interim field as a proxy for the same computation
     */
    @Test
    @Ignore
    public void equivalent_predicates_with_function_calls_should_produce_the_same_results() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group jr:template=\"\"", t("number")),
                        t("count"),
                        t("result_1"),
                        t("result_2")
                    )),
                    bind("/data/group/number").type("int").required(),
                    bind("/data/count").type("int").calculate("count(/data/group)"),
                    bind("/data/result_1").type("int").calculate("10 + /data/group[position() = /data/count]/number"),
                    bind("/data/result_2").type("int").calculate("10 + /data/group[position() = count(/data/group)]/number")
                )
            ),
            body(group("/data/group", repeat("/data/group", input("/data/group/number"))))
        ));
        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        scenario.answer(10);

        assertThat(scenario.answerOf("/data/count"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/result_1"), is(intAnswer(20)));
        assertThat(scenario.answerOf("/data/result_2"), is(intAnswer(20)));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        scenario.answer(20);

        assertThat(scenario.answerOf("/data/count"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/result_1"), is(intAnswer(30)));
        // The following assertion is the one that fails with the current implementation
        // TODO Explore the difference between the calculations in result_1 and result_2 and unify them
        assertThat(scenario.answerOf("/data/result_2"), is(intAnswer(30)));
    }

    @Test
    public void parsing_forms_with_cycles_by_self_reference_in_calculate_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/count").type("int").calculate(". + 1")
        ));
    }

    @Test
    public void parsing_forms_with_cycles_in_calculate_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/a").type("int").calculate("/data/b + 1"),
            bind("/data/b").type("int").calculate("/data/c + 1"),
            bind("/data/c").type("int").calculate("/data/a + 1")
        ));
    }

    @Test
    public void parsing_forms_with_cycles_by_self_reference_in_relevance_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/count").type("int").relevant(". > 0")
        ));
    }

    @Test
    public void parsing_forms_with_cycles_by_self_reference_in_read_only_condition_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/count").type("int").readonly(". > 10")
        ));
    }

    @Test
    public void parsing_forms_with_cycles_by_self_reference_in_required_condition_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/count").type("int").required(". > 10")
        ));
    }

    @Test
    public void supports_self_references_in_constraints() throws IOException {
        Scenario scenario = Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/count").type("int").constraint(". > 10")
        ));
        scenario.next();
        scenario.answer(5);
        assertThat(scenario.answerOf("/data/count"), is(nullValue()));
        scenario.answer(20);
        assertThat(scenario.answerOf("/data/count"), is(intAnswer(20)));
        scenario.answer(5);
        assertThat(scenario.answerOf("/data/count"), is(intAnswer(20)));
    }

    /**
     * This test is here to represent a use case that might seem like it
     * has a cycle, but it doesn't.
     * <p>
     * This test is ignored because the current implementation incorrectly
     * detects a cycle given the relevance conditions we have used. Once
     * this is fixed, this test would be a regression test to ensure we
     * never rollback on the fix.
     * <p>
     * The relevance conditions used here a co-dependant(field a depends
     * on b, b depends on a), but they depend on the field's value, not on
     * the field's relevance expression. This is why there's no cycle here.
     * <p>
     * To have a cycle using relevance conditions exclusively, we would need
     * a isRelevant() xpath function that doesn't exist and change the revelance
     * expressions to:
     *
     * <code>
     * bind("/data/a").type("int").relevant("isRelevant(/data/b) > 0")
     * bind("/data/b").type("int").relevant("isRelevant(/data/a) > 0")
     * </code>
     */
    @Test
    @Ignore
    public void supports_codependant_relevant_expressions() throws IOException {
        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/a").type("int").relevant("/data/b > 0"),
            bind("/data/b").type("int").relevant("/data/a > 0")));
        // TODO Complete the test adding some assertions that verify that the form works as we would expect
    }

    /**
     * This test is here to represent a use case that might seem like it
     * has a cycle, but it doesn't.
     * <p>
     * This test is ignored because the current implementation incorrectly
     * detects a cycle given the required conditions we have used. Once
     * this is fixed, this test would be a regression test to ensure we
     * never rollback on the fix.
     * <p>
     * The required conditions used here a co-dependant(field a depends
     * on b, b depends on a), but they depend on the field's value, not on
     * the field's required conditions. This is why there's no cycle here.
     * <p>
     * To have a cycle using required conditions exclusively, we would need
     * a isRequired() xpath function that doesn't exist and change the required
     * expressions to:
     *
     * <code>
     * bind("/data/a").type("int").required("isRequired(/data/b) > 0")
     * bind("/data/b").type("int").required("isRequired(/data/a) > 0")
     * </code>
     */
    @Test
    @Ignore
    public void supports_codependant_required_conditions() throws IOException {
        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/a").type("int").required("/data/b > 0"),
            bind("/data/b").type("int").required("/data/a > 0")));
        // TODO Complete the test adding some assertions that verify that the form works as we would expect
    }

    /**
     * This test is here to represent a use case that might seem like it
     * has a cycle, but it doesn't.
     * <p>
     * This test is ignored because the current implementation incorrectly
     * detects a cycle given the readonly conditions we have used. Once
     * this is fixed, this test would be a regression test to ensure we
     * never rollback on the fix.
     * <p>
     * The readonly conditions used here a co-dependant(field a depends
     * on b, b depends on a), but they depend on the field's value, not on
     * the field's readonly conditions. This is why there's no cycle here.
     * <p>
     * To have a cycle using readonly conditions exclusively, we would need
     * a isReadonly() xpath function that doesn't exist and change the readonly
     * expressions to:
     *
     * <code>
     * bind("/data/a").type("int").readonly("isReadonly(/data/b) > 0")
     * bind("/data/b").type("int").readonly("isReadonly(/data/a) > 0")
     * </code>
     */
    @Test
    @Ignore
    public void supports_codependant_readonly_conditions() throws IOException {
        Scenario.init("Some form", buildFormForDagCyclesCheck(
            bind("/data/a").type("int").readonly("/data/b > 0"),
            bind("/data/b").type("int").readonly("/data/a > 0")));
        // TODO Complete the test adding some assertions that verify that the form works as we would expect
    }

    @Test
    public void parsing_forms_with_cycles_involving_fields_inside_and_outside_of_repeat_groups_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group", t("a", "1")),
                        t("b", "1")
                    )),
                    bind("/data/group/a").type("int").calculate("/data/b + 1"),
                    bind("/data/b").type("int").calculate("/data/group[position() = 1]/a + 1")
                )
            ),
            body(
                group("/data/group",
                    repeat("/data/group",
                        input("/data/group/a")
                    )
                ),
                input("/data/b")
            )
        ));
    }

    @Test
    public void parsing_forms_with_self_reference_cycles_in_fields_of_repeat_groups_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group",
                            t("a", "1")
                        )
                    )),
                    bind("/data/group/a").type("int").calculate("../a + 1")
                )
            ),
            body(group("/data/group", repeat("/data/group",
                input("/data/group/a")
            )))
        ));
    }

    /**
     * This test fails to parse the form because it thinks there's a
     * self-reference cycle in /data/group/a,
     * but this would be incorrect because each it depends on the same field
     * belonging to the previous
     * repeat instance, which wouldn't be a cycle, but an autoincremental
     * feature.
     */
    @Test
    @Ignore
    public void supports_self_reference_dependency_when_targeting_different_repeat_instance_siblings() throws IOException {
        Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group",
                            t("a", "1")
                        )
                    )),
                    bind("/data/group/a").type("int").calculate("/data/group[position() = (position(current()) - 1)]/a + 1")
                )
            ),
            body(group("/data/group", repeat("/data/group",
                input("/data/group/a")
            )))
        ));
    }


    @Test
    public void parsing_forms_with_cycles_between_fields_of_the_same_repeat_instance_should_fail() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Cycle detected in form's relevant and calculation logic!");

        Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group",
                            t("a", "1"),
                            t("b", "1")
                        )
                    )),
                    bind("/data/group/a").type("int").calculate("../b + 1"),
                    bind("/data/group/b").type("int").calculate("../a + 1")
                )
            ),
            body(group("/data/group", repeat("/data/group",
                input("/data/group/a"),
                input("/data/group/b")
            )))
        ));
    }

    @Test
    public void order_of_the_DAG_is_ensured() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("a", "2"),
                        t("b"),
                        t("c")
                    )),
                    bind("/data/a").type("int"),
                    bind("/data/b").type("int").calculate("/data/a * 3"),
                    bind("/data/c").type("int").calculate("(/data/a + /data/b) * 5")
                )
            ),
            body(input("/data/a"))
        ));

        assertThat(scenario.answerOf("/data/a"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/b"), is(intAnswer(6)));
        assertThat(scenario.answerOf("/data/c"), is(intAnswer(40)));

        scenario.answer("/data/a", 3);

        assertThat(scenario.answerOf("/data/a"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/b"), is(intAnswer(9)));
        // Verify that c gets computed using the updated value of b.
        assertThat(scenario.answerOf("/data/c"), is(intAnswer(60)));
    }

    private void assertDagEvents(List<Event> dagEvents, String... lines) {
        assertThat(dagEvents.stream().map(Event::getDisplayMessage).collect(joining("\n")), is(join("\n", lines)));
    }

    public XFormsElement buildFormForDagCyclesCheck(BindBuilderXFormsElement... binds) {
        return buildFormForDagCyclesCheck(null, binds);
    }

    public XFormsElement buildFormForDagCyclesCheck(String initialValue, BindBuilderXFormsElement... binds) {
        // Map the last part of each bind's nodeset to model fields
        // They will get an initial value if provided
        List<XFormsElement> modelFields = Stream.of(binds)
            .map(bind -> {
                String[] parts = bind.getNodeset().split("/");
                return parts[parts.length - 1];
            })
            .map(name -> {
                if (initialValue == null)
                    return t(name);
                return t(name, initialValue);
            })
            .collect(toList());

        // Build the main instance with the model fields we've just built
        XFormsElement mainInstance = mainInstance(t("data id=\"some-form\"", modelFields.toArray(new XFormsElement[]{})));

        // Build the model including the main instance we've just built and the provided binds
        List<XFormsElement> modelChildren = new LinkedList<>();
        modelChildren.add(mainInstance);
        modelChildren.addAll(Arrays.asList(binds));

        // Map each bind's nodeset to body fields (inputs)
        List<XFormsElement> inputs = Stream.of(binds)
            .map(BindBuilderXFormsElement::getNodeset)
            .map(name -> input(name))
            .collect(toList());

        // Return the complete form including model fields, binds, and body inputs
        return html(head(
            title("Some form"),
            model(modelChildren.toArray(new XFormsElement[]{}))
        ), body(inputs.toArray(new XFormsElement[]{})));
    }

    // TODO Replace this test with a benchmark
    @Test
    public void deleteRepeatGroupWithCalculationsTimingTest() throws Exception {
        // Given
        FormDef formDef = parse(r("delete-repeat-group-with-calculations-timing-test.xml"));

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations

        FormInstance mainInstance = formDef.getMainInstance();

        // Construct the required amount of repeats
        TreeElement templateRepeat = mainInstance.getRoot().getChildAt(0);
        int numberOfRepeats = 10; // Raise this value to really measure
        for (int i = 0; i < numberOfRepeats; i++) {
            TreeReference refToNewRepeat = templateRepeat.getRef();
            refToNewRepeat.setMultiplicity(1, i); // set the correct multiplicity

            FormIndex indexOfNewRepeat = new FormIndex(0, i, refToNewRepeat);
            formDef.createNewRepeat(indexOfNewRepeat);
        }

        TreeElement firstRepeat = mainInstance.getRoot().getChildAt(1);
        TreeReference firstRepeatRef = firstRepeat.getRef();
        FormIndex firstRepeatIndex = new FormIndex(0, 0, firstRepeatRef);

        // When
        long start = System.nanoTime();

        for (int i = 0; i < numberOfRepeats; i++) {
            long currentIterationStart = System.nanoTime();
            formDef.deleteRepeat(firstRepeatIndex);
            double tookMs = (System.nanoTime() - currentIterationStart) / 1000000D;
            logger.info(format("%d\t%.3f\n", i, tookMs));
        }

        // Then
        LocalTime duration = LocalTime.fromMillisOfDay((System.nanoTime() - start) / 1_000_000);
        logger.info("Deletion of {} repeats took {}", numberOfRepeats, duration.toString());
    }


}
