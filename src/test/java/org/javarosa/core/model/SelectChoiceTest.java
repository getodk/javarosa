/*
 * Copyright 2020 Nafundi
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

package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.label;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.select1Dynamic;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.javarosa.test.utils.ResourcePathHelper.r;

import java.io.IOException;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.junit.Test;

public class SelectChoiceTest {
    @Test
    public void value_should_continue_being_an_empty_string_after_deserialization() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("SelectChoice.getValue() regression test form", html(
            head(
                title("SelectChoice.getValue() regression test form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("the-choice")
                    )),
                    bind("/data/the-choice").type("string").required()
                )
            ),
            body(select1("/data/the-choice",
                label("Select one choice"),
                item("", "Empty value")
            ))
        ));

        scenario.next();
        assertThat(scenario.getQuestionAtIndex().getChoice(0).getValue(), is(""));

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.newInstance();
        deserializedScenario.next();
        assertThat(deserializedScenario.getQuestionAtIndex().getChoice(0).getValue(), is(""));
    }

    @Test
    public void getChild_returnsNamedChild_whenChoicesAreFromSecondaryInstance() {
        setUpSimpleReferenceManager(r("external-select-geojson.xml").getParent(), "file");

        Scenario scenario = Scenario.init("external-select-geojson.xml");
        assertThat(scenario.choicesOf("/data/q").get(1).getChild("geometry"), CoreMatchers.is("0.5 104 0 0"));
        assertThat(scenario.choicesOf("/data/q").get(1).getChild("special-property"), CoreMatchers.is("special value"));
    }

    @Test
    public void getChild_returnsNull_whenChoicesAreFromSecondaryInstance_andRequestedChildDoesNotExist() {
        setUpSimpleReferenceManager(r("external-select-geojson.xml").getParent(), "file");

        Scenario scenario = Scenario.init("external-select-geojson.xml");
        assertThat(scenario.choicesOf("/data/q").get(1).getChild("non-existent"), CoreMatchers.is(nullValue()));
    }

    @Test
    public void getChild_updates_whenChoicesAreFromRepeat() throws IOException {
        Scenario scenario = Scenario.init("Select from repeat", html(
            head(
                title("Select from repeat"),
                model(
                    mainInstance(
                        t("data id='repeat-select'",
                            t("repeat",
                                t("value"),
                                t("label"),
                                t("special-property")),
                            t("filter"),
                            t("select"))))),
            body(
                repeat("/data/repeat",
                    input("value"),
                    input("label"),
                    input("special-property")),
                input("filter"),
                select1Dynamic("/data/select", "../repeat")
            )));
        scenario.answer("/data/repeat[0]/value", "a");
        scenario.answer("/data/repeat[0]/label", "A");
        scenario.answer("/data/repeat[0]/special-property", "AA");

        assertThat(scenario.choicesOf("/data/select").get(0).getValue(), is("a"));
        assertThat(scenario.choicesOf("/data/select").get(0).getChild("special-property"), is("AA"));

        scenario.answer("/data/repeat[0]/special-property", "changed");
        assertThat(scenario.choicesOf("/data/select").get(0).getChild("special-property"), is("changed"));
    }

    @Test
    public void getChild_returnsNull_whenCalledOnAChoiceFromInlineSelect() throws IOException {
        Scenario scenario = Scenario.init("Static select", html(
            head(
                title("Static select"),
                model(
                    mainInstance(
                        t("data id='static-select'",
                            t("select"))))),
            body(
                select1("/data/select", item("one", "One"), item("two", "Two"))
            )));

        assertThat(scenario.choicesOf("/data/select").get(0).getChild("invalid-property"), nullValue());
    }

    @Test
    public void getAdditionalChildren_returnsChildren_whenChoicesAreFromSecondaryInstance() {
        setUpSimpleReferenceManager(r("external-select-geojson.xml").getParent(), "file");

        Scenario scenario = Scenario.init("external-select-geojson.xml");

        Map<String, String> firstNodeChildren = scenario.choicesOf("/data/q").get(0).getAdditionalChildren();
        assertThat(firstNodeChildren.keySet(), hasSize(3));
        assertThat(firstNodeChildren, hasEntry("geometry", "0.5 102 0 0"));
        assertThat(firstNodeChildren, hasEntry("id", "fs87b"));
        assertThat(firstNodeChildren, hasEntry("foo", "bar"));

        Map<String, String> secondNodeChildren = scenario.choicesOf("/data/q").get(1).getAdditionalChildren();
        assertThat(secondNodeChildren.keySet(), hasSize(4));
        assertThat(secondNodeChildren, hasEntry("geometry", "0.5 104 0 0"));
        assertThat(secondNodeChildren, hasEntry("id", "67abie"));
        assertThat(secondNodeChildren, hasEntry("foo", "quux"));
        assertThat(secondNodeChildren, hasEntry("special-property", "special value"));
    }

    @Test
    public void getChildren_updates_whenChoicesAreFromRepeat() throws IOException {
        Scenario scenario = Scenario.init("Select from repeat", html(
            head(
                title("Select from repeat"),
                model(
                    mainInstance(
                        t("data id='repeat-select'",
                            t("repeat",
                                t("value"),
                                t("label"),
                                t("special-property")),
                            t("filter"),
                            t("select"))))),
            body(
                repeat("/data/repeat",
                    input("value"),
                    input("label"),
                    input("special-property")),
                input("filter"),
                select1Dynamic("/data/select", "../repeat")
            )));
        scenario.answer("/data/repeat[0]/value", "a");
        scenario.answer("/data/repeat[0]/label", "A");
        scenario.answer("/data/repeat[0]/special-property", "AA");

        assertThat(scenario.choicesOf("/data/select").get(0).getValue(), is("a"));
        Map<String, String> children = scenario.choicesOf("/data/select").get(0).getAdditionalChildren();
        assertThat(children.keySet(), hasSize(2));
        assertThat(children, hasEntry("value", "a"));
        assertThat(children, hasEntry("special-property", "AA"));

        scenario.answer("/data/repeat[0]/special-property", "changed");
        children = scenario.choicesOf("/data/select").get(0).getAdditionalChildren();
        assertThat(children, hasEntry("special-property", "changed"));
    }

    @Test
    public void getAdditionalChildren_returnsEmpty_whenCalledOnAChoiceFromInlineSelect() throws IOException {
        Scenario scenario = Scenario.init("Static select", html(
            head(
                title("Static select"),
                model(
                    mainInstance(
                        t("data id='static-select'",
                            t("select"))))),
            body(
                select1("/data/select", item("one", "One"), item("two", "Two"))
            )));

        assertThat(scenario.choicesOf("/data/select").get(0).getAdditionalChildren().keySet(), is(empty()));
    }
}
