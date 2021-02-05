package org.javarosa.core.model.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
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
}
