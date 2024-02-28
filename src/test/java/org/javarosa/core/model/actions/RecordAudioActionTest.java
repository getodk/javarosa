/*
 * Copyright 2021 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActions;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class RecordAudioActionTest {
    @Test
    public void recordAudioAction_isProcessedOnFormParse() throws IOException, XFormParser.ParseException {
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
    public void recordAudioAction_callsListenerActionTriggeredWhenTriggered() throws IOException, XFormParser.ParseException {
        CapturingRecordAudioActionListener listener = new CapturingRecordAudioActionListener();
        RecordAudioActions.setRecordAudioListener(listener);

        Scenario.init("Record audio form", html(
            head(
                title("Record audio form"),
                model(
                    mainInstance(
                        t("data id=\"record-audio-form\"",
                            t("recording"),
                            t("q1")
                        )),
                    t("odk:recordaudio event=\"odk-instance-load\" ref=\"/data/recording\" odk:quality=\"foo\""))),
            body(
                input("/data/q1")
            )
        ));

        assertThat(listener.getAbsoluteTargetRef(), is(getRef("/data/recording")));
        assertThat(listener.getQuality(), is("foo"));
    }

    @Test
    public void targetReferenceInRepeat_isContextualized() throws IOException, XFormParser.ParseException {
        CapturingRecordAudioActionListener listener = new CapturingRecordAudioActionListener();
        RecordAudioActions.setRecordAudioListener(listener);

        Scenario.init("Record audio form", html(
            head(
                title("Record audio form"),
                model(
                    mainInstance(
                        t("data id=\"record-audio-form\"",
                            t("repeat",
                                t("recording"),
                                t("q1"))
                        )))),
            body(
                repeat("/data/repeat",
                    t("odk:recordaudio event=\"odk-instance-load\" ref=\"/data/repeat/recording\""),
                    input("/data/repeat/q1"))
            )
        ));

        assertThat(listener.getAbsoluteTargetRef(), is(getRef("/data/repeat[1]/recording")));
    }

    @Test
    public void serializationAndDeserialization_maintainsFields() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Record audio form", html(
            head(
                title("Record audio form"),
                model(
                    mainInstance(
                        t("data id=\"record-audio-form\"",
                            t("recording"),
                            t("q1")
                        )),
                    t("odk:recordaudio event=\"odk-instance-load\" ref=\"/data/recording\" odk:quality=\"foo\""))),
            body(
                input("/data/q1")
            )
        ));

        CapturingRecordAudioActionListener listener = new CapturingRecordAudioActionListener();
        RecordAudioActions.setRecordAudioListener(listener);

        scenario.serializeAndDeserializeForm();

        assertThat(listener.getAbsoluteTargetRef(), is(getRef("/data/recording")));
        assertThat(listener.getQuality(), is("foo"));
    }
}
