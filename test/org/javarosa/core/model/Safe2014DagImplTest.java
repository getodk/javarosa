package org.javarosa.core.model;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertThat;

public class Safe2014DagImplTest {

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
                parse(r("repeat-group-with-children-position-calculation.xml")).formDef;

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

//        assertThat(dagEvents.size(), equalTo(expectedMessages.length));
//
//        int messageIndex = 0;
//        for (String expectedMessage : expectedMessages) {
//            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
//        }
    }

    @Test
    public void deleteSecondRepeatGroup_evaluatesTriggerables_dependentOnTheParentPosition() throws Exception {
        // Given
        final FormDef formDef =
                parse(r("repeat-group-with-children-calculations-dependent-on-the-parent.xml")).formDef;

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

//        assertThat(dagEvents.size(), equalTo(expectedMessages.length));
//
//        int messageIndex = 0;
//        for (String expectedMessage : expectedMessages) {
//            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
//        }
    }

    @Test
    public void deleteSecondRepeatGroup_doesNotEvaluateTriggerables_notDependentOnTheParentPosition() throws Exception {
        // Given
        final FormDef formDef =
                parse(r("repeat-group-with-children-calculations-not-dependent-on-the-parent.xml")).formDef;

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

//        assertThat(dagEvents.size(), equalTo(expectedMessages.length));
//
//        int messageIndex = 0;
//        for (String expectedMessage : expectedMessages) {
//            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
//        }
    }

    @Test
    public void deleteThirdRepeatGroup_evaluatesTriggerables_dependentOnTheRepeatGroupsNumber() throws Exception {
        // Given
        final FormDef formDef =
                parse(r("calculation-dependent-on-the-repeat-groups-number.xml")).formDef;

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

//        assertThat(dagEvents.size(), equalTo(expectedMessages.length));
//
//        int messageIndex = 0;
//        for (String expectedMessage : expectedMessages) {
//            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
//        }
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
                parse(r("calculation-indirectly-dependent-on-the-repeat-groups-number.xml")).formDef;

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

//        assertThat(dagEvents.size(), equalTo(expectedMessages.length));
//
//        int messageIndex = 0;
//        for (String expectedMessage : expectedMessages) {
//            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
//        }
    }

    @Test
    public void deleteRepeatGroupWithCalculationsTimingTest() throws Exception {
        // Given
        final FormDef formDef =
                parse(r("delete-repeat-group-with-calculations-timing-test.xml")).formDef;

        assertIDagImplUnderTest(formDef);

        formDef.initialize(false, new InstanceInitializationFactory()); // trigger all calculations

        final FormInstance mainInstance = formDef.getMainInstance();

        // Construct the required amount of repeats
        final TreeElement templateRepeat = mainInstance.getRoot().getChildAt(0);
        final int numberOfRepeats = 200; // Raise this value to really measure
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
        long startMs = System.currentTimeMillis();

        for (int i = 0; i < numberOfRepeats; i++) {
            long currentIterationStart = System.nanoTime();
            formDef.deleteRepeat(firstRepeatIndex);
            double tookMs = (System.nanoTime() - currentIterationStart) / 1000000D;
            System.out.printf("%d\t%.3f\n", i, tookMs);
        }

        // Then
        final String elapsedFormatted = LocalTime.fromMillisOfDay(System.currentTimeMillis() - startMs).toString();
        System.out.println("Deletion of " + numberOfRepeats + " repeats took " + elapsedFormatted);
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