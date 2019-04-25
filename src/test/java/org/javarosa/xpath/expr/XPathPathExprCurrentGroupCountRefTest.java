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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.javarosa.xpath.expr.AnswerDataMatchers.stringAnswer;
import static org.junit.Assert.assertThat;

import org.javarosa.core.test.Scenario;
import org.junit.Before;
import org.junit.Test;

public class XPathPathExprCurrentGroupCountRefTest {
    private Scenario scenario;

    @Before
    public void setUp() {
        scenario = Scenario.init("relative-current-ref-group-count-ref.xml");
    }

    @Test
    public void current_in_repeat_count_should_work_as_expected() {
        // Since the form sets a count of 3 repeats, we should be at the end of the
        // form after answering three times
        scenario
            .jumpToFirst("name").answer("Janet")
            .next("name").answer("Bob")
            .next("name").answer("Kim")
            .next();

        assertThat(scenario.answerOf("/data/my_group[0]/name"), is(stringAnswer("Janet")));
        assertThat(scenario.answerOf("/data/my_group[1]/name"), is(stringAnswer("Bob")));
        assertThat(scenario.answerOf("/data/my_group[2]/name"), is(stringAnswer("Kim")));
        assertThat(scenario.atTheEndOfForm(), is(true));
        assertThat(scenario.repeatInstancesOf("/data/my_group"), hasSize(3));
    }

}
