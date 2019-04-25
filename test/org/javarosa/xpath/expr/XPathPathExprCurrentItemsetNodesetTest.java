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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.javarosa.xpath.expr.SelectChoiceMatchers.choice;
import static org.junit.Assert.assertThat;

public class XPathPathExprCurrentItemsetNodesetTest {
    private Scenario scenario;

    @Before
    public void setUp() {
        scenario = Scenario.init("relative-current-ref-itemset-nodeset.xml");
    }

    @Test
    public void current_in_itemset_nodeset_should_refer_to_node() {
        // The choices for /data/selected_person are taken from the repeat group at /data/people
        // First We insert two items
        scenario.answer("/data/repeat_group[0]/people[0]/person", "Bob");
        scenario.answer("/data/repeat_group[0]/people[1]/person", "Janet");
        scenario.answer("/data/repeat_group[1]/people[0]/person", "Phil");
        scenario.answer("/data/repeat_group[1]/people[1]/person", "Rose");
        scenario.answer("/data/repeat_group[1]/people[2]/person", "Ada");

        // Then we check that the choices are what we expect
        // The value of each item is the position that item holds inside the repeat group
        assertThat(
            scenario.choicesOf("/data/repeat_group[0]/selected_person"),
            containsInAnyOrder(
                choice("1", "Bob"),
                choice("2", "Janet")
            )
        );

        assertThat(
            scenario.choicesOf("/data/repeat_group[1]/selected_person"),
            containsInAnyOrder(
                choice("1", "Phil"),
                choice("2", "Rose"),
                choice("3", "Ada")
            )
        );
    }
}
