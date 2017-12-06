package org.javarosa.core.model;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
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

        assertThat(dagEvents.size(), equalTo(expectedMessages.length));

        int messageIndex = 0;
        for (String expectedMessage : expectedMessages) {
            assertThat(dagEvents.get(messageIndex++).getDisplayMessage(), equalTo(expectedMessage));
        }
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