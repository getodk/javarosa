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

import org.javarosa.core.model.SelectChoice;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.javarosa.xpath.expr.AnswerDataMatchers.answer;
import static org.javarosa.xpath.expr.SelectChoiceMatchers.choice;
import static org.junit.Assert.assertThat;

import org.javarosa.core.test.Scenario;

public class XPathPathExprCurrentTest {
    private Scenario scenario;

    @Before
    public void setUp() {
        scenario = Scenario.init("relative-current-ref.xml");
    }

    /**
     * current() in a calculate should refer to the node it is in (in this case, /data/my_group/name_relative).
     * This means that to refer to a sibling node, the path should be current()/../<name of sibling node>. This is
     * verified by changing the value of the node that the calculate is supposed to refer to
     * (/data/my_group/name) and seeing that the dependent calculate is updated accordingly.
     */
    @Test
    public void current_as_calculate_root_should_refer_to_its_bound_nodeset() {
        scenario.answer("/data/my_group/name", "Bob");

        // The binding of /data/my_group/name_relative is:
        //   <bind calculate="current()/../name" nodeset="/data/my_group/name_relative" type="string"/>
        // That will copy the value of our previous answer to /data/my_group/name
        assertThat(
            scenario.answerOf("/data/my_group/name_relative"),
            is(answer(scenario.answerOf("/data/my_group/name")))
        );
    }


    /**
     * current() in a choice filter should refer to the select node the choice filter is called from, NOT the expression
     * it is in. See https://developer.mozilla.org/en-US/docs/Web/XPath/Functions/current -- this is the difference
     * between current() and .
     * <p>
     * The behavior of current() in a choice filter is verified by selecting a value for a first, static select and then
     * using that value to filter a second, dynamic select.
     */
    @Test
    public void current_as_itemset_choice_filter_root_should_refer_to_the_select_node() {
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
