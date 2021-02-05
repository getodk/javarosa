package org.javarosa.core.model.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.Scenario.getRef;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActionHandler;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.junit.Test;

public class RecordAudioActionTest {
    @Test
    public void recordAudioAction_isProcessedOnFormParse() throws IOException {
        Scenario scenario = Scenario.init("Record audio form", html(
            head(
                title("Record audio form"),
                model(
                    mainInstance(
                        t("data id=\"record-audio-form\"",
                            t("recording"),
                            t("q1")
                        )),
                    t("odk:recordaudio event=\"odk-instance-load\" ref=\"/data/recording\""))),
            body(
                input("/data/q1")
            )
        ));

        assertThat(scenario.getFormDef().hasAction("recordaudio"), is(true));
    }

    @Test
    public void recordAudioAction_callsListenerActionTriggeredWhenTriggered() throws IOException {
        CapturingXFormsActionListener listener = new CapturingXFormsActionListener();
        Actions.registerActionListener(RecordAudioActionHandler.ELEMENT_NAME, listener);

        Scenario.init("Record audio form", html(
            head(
                title("Record audio form"),
                model(
                    mainInstance(
                        t("data id=\"record-audio-form\"",
                            t("recording"),
                            t("q1")
                        )),
                    t("odk:recordaudio event=\"odk-instance-load\" ref=\"/data/recording\""))),
            body(
                input("/data/q1")
            )
        ));

        assertThat(listener.getActionName(), is(RecordAudioActionHandler.ELEMENT_NAME));
        assertThat(listener.getAbsoluteTargetRef(), is(getRef("/data/recording")));
    }

    @Test
    public void serializationAndDeserialization_maintainsFields() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("Record audio form", html(
            head(
                title("Record audio form"),
                model(
                    mainInstance(
                        t("data id=\"record-audio-form\"",
                            t("recording"),
                            t("q1")
                        )),
                    t("odk:recordaudio event=\"odk-instance-load\" ref=\"/data/recording\""))),
            body(
                input("/data/q1")
            )
        ));

        CapturingXFormsActionListener listener = new CapturingXFormsActionListener();
        Actions.registerActionListener(RecordAudioActionHandler.ELEMENT_NAME, listener);

        scenario.serializeAndDeserializeForm();

        assertThat(listener.getActionName(), is(RecordAudioActionHandler.ELEMENT_NAME));
        assertThat(listener.getAbsoluteTargetRef(), is(getRef("/data/recording")));
    }
}
