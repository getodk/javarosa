/*
 * Copyright (C) 2009 JavaRosa
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

package org.javarosa.core.model.test;

import static org.hamcrest.Matchers.hasItem;
import static org.javarosa.core.test.Scenario.AnswerResult.CONSTRAINT_VIOLATED;
import static org.javarosa.core.test.Scenario.AnswerResult.OK;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

/**
 * See testAnswerConstraint() for an example of how to write the
 * constraint unit type tests.
 */
public class FormDefTest {
    @Test
    public void enforces_constraints_defined_in_a_field() {
        Scenario scenario = Scenario.init(r("ImageSelectTester.xhtml"));
        scenario.next();
        scenario.next();
        scenario.next();
        scenario.next();
        scenario.next();
        assertThat(scenario.answer("10"), Matchers.is(CONSTRAINT_VIOLATED));
        assertThat(scenario.answer("13"), Matchers.is(OK));
    }

    @Test
    public void knows_how_to_return_a_list_of_functions_without_handler() {
        FormDef formDef = new FormParseInit(r("custom-function-form.xml")).getFormDef();
        assertThat(formDef.getFunctionsWithoutHandlers(), hasItem("trim"));
    }
}
