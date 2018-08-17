/*
 * Copyright 2018 Nafundi
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
package org.javarosa.xpath.expr;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.javarosa.xpath.expr.AnswerDataMatchers.answer;
import static org.javarosa.xpath.expr.SelectChoiceMatchers.choice;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.javarosa.core.model.SelectChoice;
import org.junit.Before;
import org.junit.Test;

public class XPathPathExprCurrentTest {
    private Scenario scenario;

    @Before
    public void setUp() {
        scenario = Scenario.init("relative-current-ref.xml");
    }

    @Test
    public void current_in_calculate_should_refer_to_node() {
        scenario.answer("/data/my_group/name", "Bob");

        // The binding of /data/my_group/name_relative is:
        //   <bind calculate="current()/../name" nodeset="/data/my_group/name_relative" type="string"/>
        // That will copy the value of our previous answer to /data/my_group/name
        assertThat(
            scenario.answerOf("/data/my_group/name_relative"),
            is(answer(scenario.answerOf("/data/my_group/name")))
        );
    }

    @Test
    public void current_in_choice_filter_should_refer_to_node() {
        scenario.answer("/data/fruit", "blueberry");
        List<SelectChoice> choices = scenario.choicesOf("/data/variety");
        // The itemset for /data/variety is instance('variety')/root/item[fruit = current()/../fruit]
        // and the "variety" instance has three items for blueberry: blueray, collins, and duke
        assertThat(choices, containsInAnyOrder(
            choice("blueray"),
            choice("collins"),
            choice("duke")
        ));
    }

}