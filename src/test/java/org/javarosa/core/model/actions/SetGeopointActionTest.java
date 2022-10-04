/*
 * Copyright 2019 Nafundi
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.test.Scenario.getRef;
import static org.javarosa.core.util.externalizable.ExtUtil.defaultPrototypes;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertThat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.Matcher;
import org.javarosa.core.model.actions.setgeopoint.SetGeopointAction;
import org.javarosa.core.model.actions.setgeopoint.StubSetGeopointAction;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class SetGeopointActionTest {
    private static final Matcher<StringData> EXPECTED_STUB_ANSWER =
        stringAnswer("no client implementation");

    @Test(expected = XFormParseException.class)
    public void when_namespaceIsNotOdk_exceptionIsThrown() throws IOException, XFormParser.ParseException {
        Scenario.init(r("setgeopoint-action-bad-namespace.xml"));
    }

    @Test
    public void when_instanceIsLoaded_locationIsSetAtTarget() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("setgeopoint-action-instance-load.xml"));

        assertThat(scenario.answerOf("/data/location"), is(EXPECTED_STUB_ANSWER));
    }

    @Test
    public void when_triggerNodeIsUpdated_locationIsSetAtTarget() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("setgeopoint-action-value-changed.xml"));

        // The test form has no default value at /data/location, and
        // no other event sets any value on it
        assert scenario.answerOf("/data/location") == null;

        // Answering a question triggers its "xforms-value-changed" event
        scenario.answer("/data/text", "some answer");

        assertThat(scenario.answerOf("/data/location"), is(EXPECTED_STUB_ANSWER));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException, DeserializationException {
        StubSetGeopointAction originalAction = new StubSetGeopointAction(getRef("/data/text"));

        Path ser = Files.createTempFile("serialized-object", null);
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(ser))) {
            originalAction.writeExternal(dos);
        }

        SetGeopointAction deserializedAction = new StubSetGeopointAction(null);
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(ser))) {
            deserializedAction.readExternal(dis, defaultPrototypes());
        }

        // SetGeopointAction only serializes the targetReference (and name, from its superclass) members
        assertThat(deserializedAction.getName(), is(originalAction.getName()));

        assertThat(deserializedAction.getTargetReference(), equalTo(originalAction.getTargetReference()));

        Files.delete(ser);
    }
}
