package org.javarosa.core.model;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;
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
import static org.javarosa.core.util.XFormsElement.label;
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
import java.util.List;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.Scenario;
import org.javarosa.debug.Event;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Safe2014DagImplTest {
    private static Logger logger = LoggerFactory.getLogger(Safe2014DagImplTest.class);

    private List<Event> dagEvents = new ArrayList<>();

    @Before
    public void setUp() {
        dagEvents.clear();
    }

    @Test
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnFollowingRepeatGroupSiblings() {
        // Set scenario and dag event listener
        Scenario scenario = Scenario
            .init("repeat-group-with-children-position-calculation.xml")
            .onDagEvent(dagEvents::add);

        // Assert initial values in the form's main instance after initialization
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(intAnswer(5)));

        // Remove second repeat
        scenario.removeRepeat("/data/house[1]");

        // Assert values after removing the second repeat
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(nullValue()));

        // Assert dag events
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
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnTheParentPosition() {
        // Set scenario and dag event listener
        Scenario scenario = Scenario
            .init("repeat-group-with-children-calculations-dependent-on-the-parent.xml")
            .onDagEvent(dagEvents::add);

        // Assert initial values in the form's main instance after initialization
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[0]/name"), is(stringAnswer("A")));
        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("A1")));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[1]/name"), is(stringAnswer("B")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("B2")));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[2]/name"), is(stringAnswer("C")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("C3")));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[3]/name"), is(stringAnswer("D")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("D4")));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(intAnswer(5)));
        assertThat(scenario.answerOf("/data/house[4]/name"), is(stringAnswer("E")));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(stringAnswer("E5")));

        // Remove second repeat
        scenario.removeRepeat("/data/house[1]");

        // Assert values after removing the second repeat: number and name_and_number should change
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[0]/name"), is(stringAnswer("A")));
        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("A1")));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[1]/name"), is(stringAnswer("C")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("C2")));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[2]/name"), is(stringAnswer("D")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("D3")));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[3]/name"), is(stringAnswer("E")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("E4")));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(nullValue()));
        assertThat(scenario.answerOf("/data/house[4]/name"), is(nullValue()));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(nullValue()));

        // Assert dag events
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
    public void deleteSecondRepeatGroup_doesNotEvaluateTriggerables_notDependentOnTheParentPosition() {
        // Set scenario and dag event listener
        Scenario scenario = Scenario
            .init("repeat-group-with-children-calculations-not-dependent-on-the-parent.xml")
            .onDagEvent(dagEvents::add);

        // Assert initial values in the form's main instance after initialization
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[0]/name"), is(stringAnswer("A")));
        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("AX")));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[1]/name"), is(stringAnswer("B")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("BX")));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[2]/name"), is(stringAnswer("C")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("CX")));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[3]/name"), is(stringAnswer("D")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("DX")));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(intAnswer(5)));
        assertThat(scenario.answerOf("/data/house[4]/name"), is(stringAnswer("E")));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(stringAnswer("EX")));

        // Remove second repeat
        scenario.removeRepeat("/data/house[1]");

        // Assert values after removing the second repeat: number should change, name_and_number shouldn't change
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[0]/name"), is(stringAnswer("A")));
        assertThat(scenario.answerOf("/data/house[0]/name_and_number"), is(stringAnswer("AX")));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[1]/name"), is(stringAnswer("C")));
        assertThat(scenario.answerOf("/data/house[1]/name_and_number"), is(stringAnswer("CX")));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[2]/name"), is(stringAnswer("D")));
        assertThat(scenario.answerOf("/data/house[2]/name_and_number"), is(stringAnswer("DX")));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[3]/name"), is(stringAnswer("E")));
        assertThat(scenario.answerOf("/data/house[3]/name_and_number"), is(stringAnswer("EX")));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(nullValue()));
        assertThat(scenario.answerOf("/data/house[4]/name"), is(nullValue()));
        assertThat(scenario.answerOf("/data/house[4]/name_and_number"), is(nullValue()));

        // Assert dag events
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
    public void deleteThirdRepeatGroup_evaluatesTriggerables_dependentOnTheRepeatGroupsNumber() {
        // Set scenario and dag event listener
        Scenario scenario = Scenario
            .init("calculation-dependent-on-the-repeat-groups-number.xml")
            .onDagEvent(dagEvents::add);

        // Assert initial values in the form's main instance after initialization
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(intAnswer(5)));
        assertThat(scenario.answerOf("/data/house[5]/number"), is(intAnswer(6)));
        assertThat(scenario.answerOf("/data/house[6]/number"), is(intAnswer(7)));
        assertThat(scenario.answerOf("/data/house[7]/number"), is(intAnswer(8)));
        assertThat(scenario.answerOf("/data/house[8]/number"), is(intAnswer(9)));
        assertThat(scenario.answerOf("/data/house[9]/number"), is(intAnswer(10)));

        // Remove third repeat
        scenario.removeRepeat("/data/house[2]");

        // Assert values after removing the third repeat
        assertThat(scenario.answerOf("/data/house[0]/number"), is(intAnswer(1)));
        assertThat(scenario.answerOf("/data/house[1]/number"), is(intAnswer(2)));
        assertThat(scenario.answerOf("/data/house[2]/number"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/house[3]/number"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/house[4]/number"), is(intAnswer(5)));
        assertThat(scenario.answerOf("/data/house[5]/number"), is(intAnswer(6)));
        assertThat(scenario.answerOf("/data/house[6]/number"), is(intAnswer(7)));
        assertThat(scenario.answerOf("/data/house[7]/number"), is(intAnswer(8)));
        assertThat(scenario.answerOf("/data/house[8]/number"), is(intAnswer(9)));
        assertThat(scenario.answerOf("/data/house[9]/number"), is(nullValue()));

        // Assert dag events
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
     * Indirectly means that the calculation - `concat(/data/house/name)` - does not take the
     * `/data/house` nodeset (the repeat group) as an argument
     * but since it takes one of its children (`name` children),
     * the calculation must re-evaluated once after a repeat group deletion because one of the children
     * has been deleted along with its parent (the repeat group instance).
     */
    @Test
    public void deleteThirdRepeatGroup_evaluatesTriggerables_indirectlyDependentOnTheRepeatGroupsNumber() {
        // Set scenario and dag event listener
        Scenario scenario = Scenario
            .init("calculation-indirectly-dependent-on-the-repeat-groups-number.xml")
            .onDagEvent(dagEvents::add);

        // Assert initial values in the form's main instance after initialization
        assertThat(scenario.answerOf("/data/house[0]/name"), is(stringAnswer("A")));
        assertThat(scenario.answerOf("/data/house[1]/name"), is(stringAnswer("B")));
        assertThat(scenario.answerOf("/data/house[2]/name"), is(stringAnswer("C")));
        assertThat(scenario.answerOf("/data/house[3]/name"), is(stringAnswer("D")));
        assertThat(scenario.answerOf("/data/house[4]/name"), is(stringAnswer("E")));
        assertThat(scenario.answerOf("/data/summary"), is(stringAnswer("ABCDE")));

        // Remove second repeat
        scenario.removeRepeat("/data/house[2]");

        // Assert values after removing the second repeat
        assertThat(scenario.answerOf("/data/house[0]/name"), is(stringAnswer("A")));
        assertThat(scenario.answerOf("/data/house[1]/name"), is(stringAnswer("B")));
        assertThat(scenario.answerOf("/data/house[2]/name"), is(stringAnswer("D")));
        assertThat(scenario.answerOf("/data/house[3]/name"), is(stringAnswer("E")));
        assertThat(scenario.answerOf("/data/house[4]/name"), is(nullValue()));
        assertThat(scenario.answerOf("/data/summary"), is(stringAnswer("ABDE")));

        // Assert dag events
        assertDagEvents(dagEvents,
            "Processing 'Deleted: house [3]: 0 triggerables were fired.' for ",
            "Processing 'Recalculate' for summary [1] (ABDE)",
            "Processing 'Deleted: name [3_1]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: house [4]: 0 triggerables were fired.' for "
        );

    }

    @Test
    public void verify_relation_between_calculate_expressions_and_relevancy_conditions() throws IOException {
        Scenario scenario = Scenario.init("Interdependencies test", html(
            head(
                title("Interdependencies test"),
                model(
                    mainInstance(
                        t("data id=\"interdependencies-test\"",
                            t("number1"),
                            t("continue"),
                            t("group", t("number1_x2"), t("number1_x2_x2"), t("number2")),
                            t("meta", t("instanceID"))
                        )
                    ),
                    bind("/data/number1").type("int").constraint(". > 0").required(),
                    bind("/data/continue").type("string").required(),
                    bind("/data/group").relevant("/data/continue = '1'"),
                    bind("/data/group/number1_x2").type("int").calculate("/data/number1 * 2"),
                    bind("/data/group/number1_x2_x2").type("int").calculate("/data/group/number1_x2 * 2"),
                    bind("/data/group/number2").type("int").relevant("/data/group/number1_x2 > 0").required(),
                    bind("/data/meta/instanceID").type("string").preload("uid").readonly()
                )
            ),
            body(
                input("/data/number1"),
                select1("/data/continue",
                    label("Continue?"),
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
        assertThat(scenario.answerOf("/data/group/number1_x2_x2"), is(nullValue()));
        scenario.next();
        scenario.answer("1"); // Label: "yes"
        assertThat(scenario.answerOf("/data/group/number1_x2"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/data/group/number1_x2_x2"), is(intAnswer(8)));
    }

    /**
     * Ignored because the assertions about non-null next-numbers will fail because our DAG
     * doesn't evaluate calculations in repeat instances that are previous siblings to the
     * one that has changed.
     */
    @Test
    @Ignore
    public void calculate_expressions_should_be_evaluated_on_previous_repeat_siblings() throws IOException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group jr:template=\"\"", t("prev-number"), t("number"), t("next-number")),
                        t("meta", t("instanceID"))
                    )),
                    bind("/data/group/prev-number").type("int").calculate("/data/group[position() = (position(current()/..) - 1)]/number"),
                    bind("/data/group/number").type("int").required(),
                    bind("/data/group/next-number").type("int").calculate("/data/group[position() = (position(current()/..) + 1)]/number"),
                    bind("/data/meta/instanceID").type("string").preload("uid").readonly()
                )
            ),
            body(
                group("/data/group",
                    repeat("/data/group",
                        input("/data/group/number")
                    )
                )
            )
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

        assertThat(scenario.answerOf("/data/group[0]/next-number"), is(intAnswer(22)));
        assertThat(scenario.answerOf("/data/group[1]/next-number"), is(intAnswer(33)));
        assertThat(scenario.answerOf("/data/group[2]/next-number"), is(nullValue()));
    }

    private void assertDagEvents(List<Event> dagEvents, String... lines) {
        assertThat(dagEvents.stream().map(Event::getDisplayMessage).collect(joining("\n")), is(join("\n", lines)));
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
