/*
 * Copyright (C) 2021 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.SelectChoiceMatchers.choice;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1Dynamic;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.XFormsElement;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration tests to verify that the choice lists for "dynamic selects" (selects with itemsets rather than inline items) are updated when
 * dependent values change.
 *
 * See also:
 * - {@see SelectOneChoiceFilterTest}
 * - {@see SelectMultipleChoiceFilterTest} for coverage of dynamic select multiples
 * - {@see XPathFuncExprRandomizeTest} for coverage of choice list updates when randomization is specified
 */
public class DynamicSelectUpdateTest {
    //region Select from repeat
    // Unlike static secondary instances, repeats are dynamic. Repeat instances (items) can be added or removed. The
    // contents of those instances (item values, labels) can also change.
    @Test
    public void selectFromRepeat_whenRepeatAdded_updatesChoices() throws Exception {
        Scenario scenario = Scenario.init("Select from repeat", getSelectFromRepeatForm());

        scenario.answer("/data/repeat[0]/value","a");
        scenario.answer("/data/repeat[0]/label","A");
        assertThat(scenario.choicesOf("/data/select"), contains(
            choice("a", "A")));

        scenario.answer("/data/repeat[1]/value","b");
        scenario.answer("/data/repeat[1]/label","B");
        assertThat(scenario.choicesOf("/data/select"), containsInAnyOrder(
            choice("a", "A"),
            choice("b", "B")));
    }

    @Test
    @Ignore("TODO")
    public void selectFromRepeat_whenRepeatChanged_updatesChoices() throws Exception {
        Scenario scenario = Scenario.init("Select from repeat", getSelectFromRepeatForm());

        scenario.answer("/data/repeat[0]/value","a");
        scenario.answer("/data/repeat[0]/label","A");
        assertThat(scenario.choicesOf("/data/select"), contains(
            choice("a", "A")));

        scenario.answer("/data/repeat[0]/value","c");
        scenario.answer("/data/repeat[0]/label","C");
        assertThat(scenario.choicesOf("/data/select"), contains(
            choice("c", "C")));
        assertThat(scenario.choicesOf("/data/select").size(), is(1));
    }

    @Test
    public void selectFromRepeat_whenRepeatRemoved_updatesChoices() throws Exception {
        Scenario scenario = Scenario.init("Select from repeat", getSelectFromRepeatForm());

        scenario.answer("/data/repeat[0]/value", "a");
        scenario.answer("/data/repeat[0]/label", "A");
        assertThat(scenario.choicesOf("/data/select"), contains(
            choice("a", "A")));

        scenario.removeRepeat("/data/repeat[0]");
        assertThat(scenario.choicesOf("/data/select").size(), is(0));
    }

    @Test
    public void selectFromRepeat_withPredicate_whenPredicateTriggerChanges_updatesChoices() throws Exception {
        Scenario scenario = Scenario.init("Select from repeat", getSelectFromRepeatForm("starts-with(value,current()/../filter)"));

        scenario.answer("/data/repeat[0]/value", "a");
        scenario.answer("/data/repeat[0]/label", "A");
        scenario.answer("/data/filter", "a");
        assertThat(scenario.choicesOf("/data/select"), contains(
            choice("a", "A")));

        scenario.answer("/data/filter", "b");
        assertThat(scenario.choicesOf("/data/select").size(), is(0));
    }

    private static XFormsElement getSelectFromRepeatForm() {
        return getSelectFromRepeatForm("");
    }

    private static XFormsElement getSelectFromRepeatForm(String predicate) {
        return html(
            head(
                title("Select from repeat"),
                model(
                    mainInstance(
                        t("data id=\"repeat-select\"",
                            t("repeat",
                                t("value"),
                                t("label")),
                            t("select"),
                            t("filter"))))),
            body(
                repeat("/data/repeat",
                    input("value"),
                    input("label")),
                select1Dynamic("/data/select", "../repeat" + (!predicate.isEmpty() ? "[" + predicate + "]" : "")),
                input("filter")
            )
        );
    }
    //endregion

    //region Select in repeat
    // When a dynamic select is in a repeat, the itemsets for all repeat instances are represented by the same ItemsetBinding.
    @Test
    public void selectInRepeat_withPredicate_evaluatesChoiceListForEachRepeatInstance() throws Exception {
        Scenario scenario = Scenario.init("Select from repeat", html(
            head(
                title("Select in repeat"),
                model(
                    mainInstance(
                        t("data id=\"repeat-select\"",
                            t("repeat",
                                t("select"),
                                t("filter")))),

                    instance("choices",
                        item("a", "A"),
                        item("aa", "AA"),
                        item("b", "B"),
                        item("bb", "BB"))),
                body(
                    repeat("/data/repeat",
                        select1Dynamic("/data/repeat/select", "instance('choices')/root/item[starts-with(value,current()/../filter)]"),
                        input("filter"))
                )
            )));

        scenario.answer("/data/repeat[0]/filter", "a");
        scenario.answer("/data/repeat[1]/filter", "a");
        assertThat(scenario.choicesOf("/data/repeat[0]/select").size(), is(2));
        assertThat(scenario.choicesOf("/data/repeat[1]/select").size(), is(2));

        scenario.answer("/data/repeat[1]/filter", "bb");
        assertThat(scenario.choicesOf("/data/repeat[0]/select").size(), is(2));
        assertThat(scenario.choicesOf("/data/repeat[1]/select").size(), is(1));
    }
    //endregion

    //region Multi-language
    @Test
    public void multilanguage() throws Exception {
        Scenario scenario = Scenario.init("Select from repeat", html(
            head(
                title("Multilingual dynamic select"),
                model(
                    t("itext",
                        t("translation lang=\"fr\"",
                            t("text id=\"choices-0\"",
                                t("value", "A (fr)")),
                            t("text id=\"choices-1\"",
                                t("value", "B (fr)")),
                            t("text id=\"choices-2\"",
                                t("value", "C (fr)"))
                        ),
                        t("translation lang=\"en\"",
                            t("text id=\"choices-0\"",
                                t("value", "A (en)")),
                            t("text id=\"choices-1\"",
                                t("value", "B (en)")),
                            t("text id=\"choices-2\"",
                                t("value", "C (en)"))
                        )),
                    mainInstance(
                        t("data id=\"multilingual-select\"",
                            t("select"))),

                    instance("choices",
                        t("item", t("itextId", "choices-0"), t("name", "a")),
                        t("item", t("itextId", "choices-1"), t("name", "b")),
                        t("item", t("itextId", "choices-2"), t("name", "c"))))),
            body(
                select1Dynamic("/data/select", "instance('choices')/root/item", "name", "jr:itext(itextId)"))
            ));

        scenario.setLanguage("en");
        assertThat(scenario.choicesOf("/data/select").size(), is(3));
        assertThat(scenario.choicesOf("/data/select"), containsInAnyOrder(
            choice("a", "choices-0"),
            choice("b", "choices-1"),
            choice("c", "choices-2")));
    }
    //endregion
}
