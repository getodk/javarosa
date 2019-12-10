package org.javarosa.core.model;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.Scenario;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
import org.joda.time.LocalTime;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Safe2014DagImplTest {
    private static final Logger logger = LoggerFactory.getLogger(Safe2014DagImplTest.class);

    private final List<Event> dagEvents = new ArrayList<>();

    private final EventNotifier eventNotifier = new EventNotifier() {

        @Override
        public void publishEvent(Event event) {
            dagEvents.add(event);
        }
    };

    @Test
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnFollowingRepeatGroupSiblings() throws Exception {
        // Given
        final FormDef formDef =
            parse(r("repeat-group-with-children-position-calculation.xml"));

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations
        formDef.setEventNotifier(eventNotifier); // it's important to set the test event notifier now to avoid storing events from the above initialization

        final FormInstance mainInstance = formDef.getMainInstance();

        final TreeElement elementToBeDeleted = mainInstance.getRoot().getChildAt(2);
        final TreeReference elementToBeDeletedRef = elementToBeDeleted.getRef();

        // Index pointing to the second repeat group
        final FormIndex indexToBeDeleted = new FormIndex(0, 1, elementToBeDeletedRef);

        // When

        // Safe2014DagImplTest.deleteRepeatGroup is called by the below method
        formDef.deleteRepeat(indexToBeDeleted);

        // Then
        final List<TreeElement> repeats = mainInstance.getRoot().getChildrenWithName("houseM");

        // check the values based on the position of the parents
        assertThat(repeats.get(0).getChildAt(0).getValue().getDisplayText(), equalTo("1"));
        assertThat(repeats.get(1).getChildAt(0).getValue().getDisplayText(), equalTo("2"));
        assertThat(repeats.get(2).getChildAt(0).getValue().getDisplayText(), equalTo("3"));
        assertThat(repeats.get(3).getChildAt(0).getValue().getDisplayText(), equalTo("4"));

        // check that correct calculations were triggered
        final String[] expectedMessages = {
            "Processing 'Recalculate' for no [2_1] (2.0)",
            "Processing 'Deleted: houseM [2]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: no [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [3_1] (3.0)",
            "Processing 'Deleted: houseM [3]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [4_1] (4.0)",
            "Processing 'Deleted: houseM [4]: 1 triggerables were fired.' for ",
        };

        assertThat(dagEvents.size(), equalTo(expectedMessages.length));

        int messageIndex = 0;
        for (String expectedMessage : expectedMessages) {
            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
        }
    }

    @Test
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnTheParentPosition() throws Exception {
        // Given
        final FormDef formDef =
            parse(r("repeat-group-with-children-calculations-dependent-on-the-parent.xml"));

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations
        formDef.setEventNotifier(eventNotifier); // it's important to set the test event notifier now to avoid storing events from the above initialization

        final FormInstance mainInstance = formDef.getMainInstance();

        final TreeElement elementToBeDeleted = mainInstance.getRoot().getChildAt(2);
        final TreeReference elementToBeDeletedRef = elementToBeDeleted.getRef();

        // Index pointing to the second repeat group
        final FormIndex indexToBeDeleted = new FormIndex(0, 1, elementToBeDeletedRef);

        // When

        // Safe2014DagImplTest.deleteRepeatGroup is called by the below method
        formDef.deleteRepeat(indexToBeDeleted);

        // Then
        final List<TreeElement> repeats = mainInstance.getRoot().getChildrenWithName("houseM");

        // check the values based on the position of the parents
        assertThat(repeats.get(0).getChildAt(0).getValue().getDisplayText(), equalTo("1"));
        assertThat(repeats.get(0).getChildAt(2).getValue().getDisplayText(), equalTo("A1"));
        assertThat(repeats.get(1).getChildAt(0).getValue().getDisplayText(), equalTo("2"));
        assertThat(repeats.get(1).getChildAt(2).getValue().getDisplayText(), equalTo("C2"));
        assertThat(repeats.get(2).getChildAt(0).getValue().getDisplayText(), equalTo("3"));
        assertThat(repeats.get(2).getChildAt(2).getValue().getDisplayText(), equalTo("D3"));
        assertThat(repeats.get(3).getChildAt(0).getValue().getDisplayText(), equalTo("4"));
        assertThat(repeats.get(3).getChildAt(2).getValue().getDisplayText(), equalTo("E4"));

        // check that correct calculations were triggered
        final String[] expectedMessages = {
            "Processing 'Recalculate' for no [2_1] (2.0)",
            "Processing 'Recalculate' for name_and_no [2_1] (C2)",
            "Processing 'Deleted: houseM [2]: 2 triggerables were fired.' for ",
            "Processing 'Deleted: no [2_1]: 0 triggerables were fired.' for ",
            "Processing 'Deleted: name [2_1]: 0 triggerables were fired.' for ",
            "Processing 'Deleted: name_and_no [2_1]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [3_1] (3.0)",
            "Processing 'Recalculate' for name_and_no [3_1] (D3)",
            "Processing 'Deleted: houseM [3]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [4_1] (4.0)",
            "Processing 'Recalculate' for name_and_no [4_1] (E4)",
            "Processing 'Deleted: houseM [4]: 2 triggerables were fired.' for ",
        };

        assertThat(dagEvents.size(), equalTo(expectedMessages.length));

        int messageIndex = 0;
        for (String expectedMessage : expectedMessages) {
            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
        }
    }

    @Test
    public void deleteSecondRepeatGroup_doesNotEvaluateTriggerables_notDependentOnTheParentPosition() throws Exception {
        // Given
        final FormDef formDef =
            parse(r("repeat-group-with-children-calculations-not-dependent-on-the-parent.xml"));

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations
        formDef.setEventNotifier(eventNotifier); // it's important to set the test event notifier now to avoid storing events from the above initialization

        final FormInstance mainInstance = formDef.getMainInstance();

        final TreeElement elementToBeDeleted = mainInstance.getRoot().getChildAt(2);
        final TreeReference elementToBeDeletedRef = elementToBeDeleted.getRef();

        // Index pointing to the second repeat group
        final FormIndex indexToBeDeleted = new FormIndex(0, 1, elementToBeDeletedRef);

        // When

        // Safe2014DagImplTest.deleteRepeatGroup is called by the below method
        formDef.deleteRepeat(indexToBeDeleted);

        // Then
        final List<TreeElement> repeats = mainInstance.getRoot().getChildrenWithName("houseM");

        // check the values based on the position of the parents
        assertThat(repeats.get(0).getChildAt(0).getValue().getDisplayText(), equalTo("1"));
        assertThat(repeats.get(0).getChildAt(2).getValue().getDisplayText(), equalTo("AX"));
        assertThat(repeats.get(1).getChildAt(0).getValue().getDisplayText(), equalTo("2"));
        assertThat(repeats.get(1).getChildAt(2).getValue().getDisplayText(), equalTo("CX"));
        assertThat(repeats.get(2).getChildAt(0).getValue().getDisplayText(), equalTo("3"));
        assertThat(repeats.get(2).getChildAt(2).getValue().getDisplayText(), equalTo("DX"));
        assertThat(repeats.get(3).getChildAt(0).getValue().getDisplayText(), equalTo("4"));
        assertThat(repeats.get(3).getChildAt(2).getValue().getDisplayText(), equalTo("EX"));

        // check that correct calculations were triggered
        final String[] expectedMessages = {
            "Processing 'Recalculate' for no [2_1] (2.0)",
            "Processing 'Deleted: houseM [2]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: no [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for name_concat [2_1] (CX)",
            "Processing 'Deleted: name [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: name_concat [2_1]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [3_1] (3.0)",
            "Processing 'Deleted: houseM [3]: 1 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [4_1] (4.0)",
            "Processing 'Deleted: houseM [4]: 1 triggerables were fired.' for "
        };

        assertThat(dagEvents.size(), equalTo(expectedMessages.length));

        int messageIndex = 0;
        for (String expectedMessage : expectedMessages) {
            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
        }
    }

    @Test
    public void deleteThirdRepeatGroup_evaluatesTriggerables_dependentOnTheRepeatGroupsNumber() throws Exception {
        // Given
        final FormDef formDef =
            parse(r("calculation-dependent-on-the-repeat-groups-number.xml"));

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations
        formDef.setEventNotifier(eventNotifier); // it's important to set the test event notifier now to avoid storing events from the above initialization

        final FormInstance mainInstance = formDef.getMainInstance();

        final TreeElement elementToBeDeleted = mainInstance.getRoot().getChildAt(2);
        final TreeReference elementToBeDeletedRef = elementToBeDeleted.getRef();

        // Index pointing to the second repeat group
        final FormIndex indexToBeDeleted = new FormIndex(0, 2, elementToBeDeletedRef);

        // When

        TreeElement summaryNode = mainInstance.getRoot().getChildrenWithName("summary").get(0);
        assertThat(summaryNode.getValue().getDisplayText(), equalTo("55")); // check the calculation result for 10 repeat groups

        // Safe2014DagImplTest.deleteRepeatGroup is called by the below method
        formDef.deleteRepeat(indexToBeDeleted);

        // Then
        final List<TreeElement> repeats = mainInstance.getRoot().getChildrenWithName("houseM");

        // check the values based on the position of the parents
        assertThat(repeats.get(0).getChildAt(0).getValue().getDisplayText(), equalTo("1"));
        assertThat(repeats.get(1).getChildAt(0).getValue().getDisplayText(), equalTo("2"));
        assertThat(repeats.get(2).getChildAt(0).getValue().getDisplayText(), equalTo("3"));
        assertThat(repeats.get(3).getChildAt(0).getValue().getDisplayText(), equalTo("4"));
        assertThat(repeats.get(4).getChildAt(0).getValue().getDisplayText(), equalTo("5"));
        assertThat(repeats.get(5).getChildAt(0).getValue().getDisplayText(), equalTo("6"));
        assertThat(repeats.get(6).getChildAt(0).getValue().getDisplayText(), equalTo("7"));
        assertThat(repeats.get(7).getChildAt(0).getValue().getDisplayText(), equalTo("8"));
        assertThat(repeats.get(8).getChildAt(0).getValue().getDisplayText(), equalTo("9"));

        assertThat(summaryNode.getValue().getDisplayText(), equalTo("45"));

        // check that correct calculations were triggered
        final String[] expectedMessages = {
            "Processing 'Recalculate' for no [3_1] (3.0)",
            "Processing 'Recalculate' for summary [1] (51.0)",
            "Processing 'Deleted: houseM [3]: 2 triggerables were fired.' for ",
            "Processing 'Deleted: no [3_1]: 0 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [4_1] (4.0)",
            "Processing 'Recalculate' for summary [1] (50.0)",
            "Processing 'Deleted: houseM [4]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [5_1] (5.0)",
            "Processing 'Recalculate' for summary [1] (49.0)",
            "Processing 'Deleted: houseM [5]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [6_1] (6.0)",
            "Processing 'Recalculate' for summary [1] (48.0)",
            "Processing 'Deleted: houseM [6]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [7_1] (7.0)",
            "Processing 'Recalculate' for summary [1] (47.0)",
            "Processing 'Deleted: houseM [7]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [8_1] (8.0)",
            "Processing 'Recalculate' for summary [1] (46.0)",
            "Processing 'Deleted: houseM [8]: 2 triggerables were fired.' for ",
            "Processing 'Recalculate' for no [9_1] (9.0)",
            "Processing 'Recalculate' for summary [1] (45.0)",
            "Processing 'Deleted: houseM [9]: 2 triggerables were fired.' for "
        };

        assertThat(dagEvents.size(), equalTo(expectedMessages.length));

        int messageIndex = 0;
        for (String expectedMessage : expectedMessages) {
            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
        }
    }

    /**
     * Indirectly means that the calculation - `concat(/rgwp/houseM/name)` - does not take the
     * `/rgwp/houseM` nodeset (the repeat group) as an argument
     * but since it takes one of its children (`name` children),
     * the calculation must re-evaluated once after a repeat group deletion because one of the children
     * has been deleted along with its parent (the repeat group instance).
     */
    @Test
    public void deleteThirdRepeatGroup_evaluatesTriggerables_indirectlyDependentOnTheRepeatGroupsNumber() throws Exception {
        // Given
        final FormDef formDef =
            parse(r("calculation-indirectly-dependent-on-the-repeat-groups-number.xml"));

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations
        formDef.setEventNotifier(eventNotifier); // it's important to set the test event notifier now to avoid storing events from the above initialization

        final FormInstance mainInstance = formDef.getMainInstance();

        final TreeElement elementToBeDeleted = mainInstance.getRoot().getChildAt(2);
        final TreeReference elementToBeDeletedRef = elementToBeDeleted.getRef();

        // Index pointing to the second repeat group
        final FormIndex indexToBeDeleted = new FormIndex(0, 2, elementToBeDeletedRef);

        // When
        TreeElement summaryNode = mainInstance.getRoot().getChildrenWithName("summary").get(0);
        assertThat(summaryNode.getValue().getDisplayText(), equalTo("ABCDE"));

        // Safe2014DagImplTest.deleteRepeatGroup is called by the below method
        formDef.deleteRepeat(indexToBeDeleted);

        // Then
        final List<TreeElement> repeats = mainInstance.getRoot().getChildrenWithName("houseM");

        assertThat(repeats.size(), equalTo(4));
        assertThat(repeats.get(0).getChildAt(0).getValue().getDisplayText(), equalTo("A"));
        assertThat(repeats.get(1).getChildAt(0).getValue().getDisplayText(), equalTo("B"));
        assertThat(repeats.get(2).getChildAt(0).getValue().getDisplayText(), equalTo("D"));
        assertThat(repeats.get(3).getChildAt(0).getValue().getDisplayText(), equalTo("E"));

        assertThat(summaryNode.getValue().getDisplayText(), equalTo("ABDE"));

        // check that correct calculations were triggered
        final String[] expectedMessages = {
            "Processing 'Deleted: houseM [3]: 0 triggerables were fired.' for ",
            "Processing 'Recalculate' for summary [1] (ABDE)",
            "Processing 'Deleted: name [3_1]: 1 triggerables were fired.' for ",
            "Processing 'Deleted: houseM [4]: 0 triggerables were fired.' for "
        };

        assertThat(dagEvents.size(), equalTo(expectedMessages.length));

        int messageIndex = 0;
        for (String expectedMessage : expectedMessages) {
            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
        }
    }

    @Test
    public void deleteRepeatGroupWithCalculationsTimingTest() throws Exception {
        // Given
        final FormDef formDef =
            parse(r("delete-repeat-group-with-calculations-timing-test.xml"));

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations

        final FormInstance mainInstance = formDef.getMainInstance();

        // Construct the required amount of repeats
        final TreeElement templateRepeat = mainInstance.getRoot().getChildAt(0);
        final int numberOfRepeats = 10; // Raise this value to really measure
        for (int i = 0; i < numberOfRepeats; i++) {
            final TreeReference refToNewRepeat = templateRepeat.getRef();
            refToNewRepeat.setMultiplicity(1, i); // set the correct multiplicity

            final FormIndex indexOfNewRepeat = new FormIndex(0, i, refToNewRepeat);
            formDef.createNewRepeat(indexOfNewRepeat);
        }

        final TreeElement firstRepeat = mainInstance.getRoot().getChildAt(1);
        final TreeReference firstRepeatRef = firstRepeat.getRef();
        final FormIndex firstRepeatIndex = new FormIndex(0, 0, firstRepeatRef);

        // When
        long start = System.nanoTime();

        for (int i = 0; i < numberOfRepeats; i++) {
            long currentIterationStart = System.nanoTime();
            formDef.deleteRepeat(firstRepeatIndex);
            double tookMs = (System.nanoTime() - currentIterationStart) / 1000000D;
            logger.info(String.format("%d\t%.3f\n", i, tookMs));
        }

        // Then
        LocalTime duration = LocalTime.fromMillisOfDay((System.nanoTime() - start) / 1_000_000);
        logger.info("Deletion of {} repeats took {}", numberOfRepeats, duration.toString());
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

    /**
     * Assert that {@param formDef} holds the expected {@link IDag} implementation.
     * The field is private in {@link FormDef} so the reflection must be used.
     */
    private void assertIDagImplUnderTest(FormDef formDef) throws NoSuchFieldException, IllegalAccessException {
        Field dagImplFromFormDef = FormDef.class.getDeclaredField("dagImpl");
        dagImplFromFormDef.setAccessible(true);
        assertThat(dagImplFromFormDef.get(formDef), instanceOf(Safe2014DagImpl.class));
    }

}
