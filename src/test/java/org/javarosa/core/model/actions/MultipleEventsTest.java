package org.javarosa.core.model.actions;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MultipleEventsTest {
    @Test
    public void nestedFirstLoadEvent_setsValue() {
        Scenario scenario = Scenario.init("multiple-events.xml");

        assertThat(scenario.answerOf("/data/nested-first-load").getDisplayText(), is("cheese"));
    }

    @Test
    public void nestedFirstLoadEventInGroup_setsValue() {
        Scenario scenario = Scenario.init("multiple-events.xml");

        assertThat(scenario.answerOf("/data/my-group/nested-first-load-in-group").getDisplayText(), is("more cheese"));
    }

    @Test
    public void serializedAndDeserializedNestedFirstLoadEvent_setsValue() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("multiple-events.xml");

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.newInstance();
        assertThat(deserializedScenario.answerOf("/data/nested-first-load").getDisplayText(), is("cheese"));
    }

    @Test
    public void serializedAndDeserializedNestedFirstLoadEventInGroup_setsValue() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("multiple-events.xml");

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.newInstance();
        assertThat(deserializedScenario.answerOf("/data/my-group/nested-first-load-in-group").getDisplayText(), is("more cheese"));
    }
}
