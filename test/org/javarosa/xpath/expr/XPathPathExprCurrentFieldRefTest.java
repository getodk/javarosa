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

import static org.hamcrest.Matchers.is;
import static org.javarosa.xpath.expr.AnswerDataMatchers.stringAnswer;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class XPathPathExprCurrentFieldRefTest {
    private Scenario scenario;

    @Before
    public void setUp() {
        scenario = Scenario.init("relative-current-ref-field-ref.xml");
    }

    @Test
    public void current_in_a_field_ref_should_be_the_same_as_a_relative_ref() {
        // The ref on /data/my_group[0]/name uses current()/name instead of an absolute path
        scenario.answer("/data/my_group[0]/name", "Bob");
        scenario.answer("/data/my_group[1]/name", "Janet");

        assertThat(
            scenario.answerOf("/data/my_group[0]/name"),
            is(stringAnswer("Bob"))
        );

        assertThat(
            scenario.answerOf("/data/my_group[1]/name"),
            is(stringAnswer("Janet"))
        );
    }
}