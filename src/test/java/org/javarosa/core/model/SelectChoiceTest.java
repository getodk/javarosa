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

import org.hamcrest.CoreMatchers;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import kotlin.Pair;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.ResourcePathHelper.r;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.instance;
import static org.javarosa.test.XFormsElement.item;
import static org.javarosa.test.XFormsElement.label;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.select1;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.junit.Assert.fail;

public class SelectChoiceTest {
    @Test
    public void value_should_continue_being_an_empty_string_after_deserialization() throws IOException, DeserializationException, XFormParser.ParseException {
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
    public void value_should_be_trimmed_when_select_choice_object_constructed() {
        SelectChoice choice = new SelectChoice(null, "Label", " value ", false);

        assertThat(choice.getValue(), is("value"));
    }

    @Test
    public void getChild_returnsNamedChild_whenChoicesAreFromSecondaryInstance() throws XFormParser.ParseException {
        setUpSimpleReferenceManager(r("external-select-geojson.xml").getParentFile(), "file");

        Scenario scenario = Scenario.init("external-select-geojson.xml");
        assertThat(scenario.choicesOf("/data/q").get(1).getChild("geometry"), CoreMatchers.is("0.5 104 0 0"));
        assertThat(scenario.choicesOf("/data/q").get(1).getChild("special-property"), CoreMatchers.is("special value"));
    }

    @Test
    public void getChild_returnsNull_whenChoicesAreFromSecondaryInstance_andRequestedChildDoesNotExist() throws XFormParser.ParseException {
        setUpSimpleReferenceManager(r("external-select-geojson.xml").getParentFile(), "file");

        Scenario scenario = Scenario.init("external-select-geojson.xml");
        assertThat(scenario.choicesOf("/data/q").get(1).getChild("non-existent"), is(nullValue()));
    }

    @Test
    public void getChild_returnsEmptyString_whenChoicesAreFromSecondaryInstance_andRequestedChildHasNoValue() throws XFormParser.ParseException, IOException {
        Scenario scenario = Scenario.init("Select with empty value", html(
            head(
                title("Select with empty value"),
                model(
                    mainInstance(
                        t("data id='select-empty'",
                            t("select"))),
                    instance("choices",
                        t("item", t("label", "Item"), t("name", "item"), t("property", ""))
                    ))),
            body(
                select1Dynamic("/data/select", "instance('choices')/root/item", "name", "label"))
        ));

        assertThat(scenario.choicesOf("/data/select").get(0).getChild("property"), is(""));
    }

    @Test
    public void getChild_updates_whenChoicesAreFromRepeat() throws IOException, XFormParser.ParseException {
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
    public void getChild_returnsNull_whenCalledOnAChoiceFromInlineSelect() throws IOException, XFormParser.ParseException {
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
    public void getAdditionalChildren_returnsChildrenInOrder_whenChoicesAreFromSecondaryInstance() throws XFormParser.ParseException {
        setUpSimpleReferenceManager(r("external-select-geojson.xml").getParentFile(), "file");

        Scenario scenario = Scenario.init("external-select-geojson.xml");

        List<Pair<String, String>> firstNodeChildren = scenario.choicesOf("/data/q").get(0).getAdditionalChildren();
        assertThat(firstNodeChildren, hasSize(3));
        assertThat(firstNodeChildren.get(0), equalTo(new Pair<>("geometry", "0.5 102 0 0")));
        assertThat(firstNodeChildren.get(1), equalTo(new Pair<>("id", "fs87b")));
        assertThat(firstNodeChildren.get(2), equalTo(new Pair<>("foo", "bar")));

        List<Pair<String, String>> secondNodeChildren = scenario.choicesOf("/data/q").get(1).getAdditionalChildren();
        assertThat(secondNodeChildren, hasSize(4));
        assertThat(secondNodeChildren.get(0), equalTo(new Pair<>("geometry", "0.5 104 0 0")));
        assertThat(secondNodeChildren.get(1), equalTo(new Pair<>("id", "67")));
        assertThat(secondNodeChildren.get(2), equalTo(new Pair<>("foo", "quux")));
        assertThat(secondNodeChildren.get(3), equalTo(new Pair<>("special-property", "special value")));
    }

    @Test
    public void getChildren_updates_whenChoicesAreFromRepeat() throws IOException, XFormParser.ParseException {
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
                    input("/data/repeat/value"),
                    input("/data/repeat/label"),
                    input("/data/repeat/special-property")),
                input("filter"),
                select1Dynamic("/data/select", "../repeat")
            )));
        scenario.answer("/data/repeat[0]/value", "a");
        scenario.answer("/data/repeat[0]/label", "A");
        scenario.answer("/data/repeat[0]/special-property", "AA");

        assertThat(scenario.choicesOf("/data/select").get(0).getValue(), is("a"));
        List<Pair<String, String>> children = scenario.choicesOf("/data/select").get(0).getAdditionalChildren();
        assertThat(children, hasSize(2));
        assertThat(children.get(0), equalTo(new Pair<>("value", "a")));
        assertThat(children.get(1), equalTo(new Pair<>("special-property", "AA")));

        scenario.answer("/data/repeat[0]/special-property", "changed");
        children = scenario.choicesOf("/data/select").get(0).getAdditionalChildren();
        assertThat(children.get(1), equalTo(new Pair<>("special-property", "changed")));
    }

    @Test
    public void selectFromRepeat_usesSpecifiedValueAndLabelRefs() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Select from repeat", html(
            head(
                title("Select from repeat"),
                model(
                    mainInstance(
                        t("data id='repeat-select'",
                            t("repeat",
                                t("first_name")),
                            t("select"))))),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/first_name")),
                select1Dynamic("/data/select", "/data/repeat[./first_name != '']", "first_name", "first_name")
            )));
        scenario.answer("/data/repeat[0]/first_name", "b");

        assertThat(scenario.choicesOf("/data/select").get(0).getValue(), is("b"));
        assertThat(scenario.choicesOf("/data/select").get(0).getLabelInnerText(), is("b"));
    }

    @Test
    public void getAdditionalChildren_returnsEmpty_whenCalledOnAChoiceFromInlineSelect() throws IOException, XFormParser.ParseException {
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

        assertThat(scenario.choicesOf("/data/select").get(0).getAdditionalChildren(), is(empty()));
    }

    @Test
    public void getAdditionalChildren_returnsEmptyStringValue_forEmptyChildren() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Internal instance select", html(
            head(
                title("Internal instance select"),
                model(
                    mainInstance(
                        t("data id='static-select'",
                            t("select")
                        )
                    ),
                    instance("instance",
                        t("item",
                            t("label", "label"),
                            t("value", "value"),
                            t("child")
                        )
                    )
                )
            ),
            body(
                select1Dynamic("/data/select", "instance('instance')/root/item")
            ))
        );

        assertThat(
            scenario.choicesOf("/data/select").get(0).getAdditionalChildren(),
            is(contains(new Pair<>("value", "value"), new Pair<>("child", "")))
        );
    }

    @Test
    public void itemsetBindingVerification_doesNotVerifySecondItem() throws IOException, XFormParser.ParseException {
        Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("first")
                    )),

                    t("instance id=\"mixed-schema\"",
                        t("root",
                            t("item",
                                t("label", "A"),
                                t("name", "a")
                            ),
                            t("item",
                                t("foo")))
                    ),

                    bind("/data/first").type("string")
                )
            ),
            body(
                // Define a select using value and label references that only exist for the first item
                select1Dynamic("/data/first", "instance('mixed-schema')/root/item", "name", "label")
            )));
    }

    @Test
    public void itemsetBindingVerification_verifiesFirstItem() throws IOException {
        try {
            Scenario.init("Some form", html(
                head(
                    title("Some form"),
                    model(
                        mainInstance(t("data id=\"some-form\"",
                            t("first")
                        )),

                        t("instance id=\"mixed-schema\"",
                            t("root",
                                t("item",
                                    t("foo", "bar")),
                                t("item",
                                    t("label", "A"),
                                    t("value", "a")
                                ))
                        ),

                        bind("/data/first").type("string")
                    )
                ),
                body(
                    // Define a select using value and label references that only exist for the second item
                    select1Dynamic("/data/first", "instance('mixed-schema')/root/item", "name", "label")
                )));
            fail("Expected XFormParseException because itemset references don't exist in external instance");
        } catch (XFormParseException e) {
            // pass
        } catch (XFormParser.ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
