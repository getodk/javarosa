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

package org.javarosa.smoketests;


import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.QuestionDefMatchers.irrelevant;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

public class WhoVATest {
    @Test
    public void regression_after_2_17_0_relevance_updates() {
        Scenario scenario = Scenario.init(r("whova_form.xml"));

        scenario.next(13);

        // region Give consent to unblock the rest of the form
        // (Id10013) [Did the respondent give consent?] ref:/data/respondent_backgr/Id10013
        scenario.next();
        scenario.answer("yes");
        // endregion

        scenario.next(5);

        // region Info on deceased
        // (Id10019) What was the sex of the deceased? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10019
        scenario.next();
        scenario.answer("female");
        // (Id10020) Is the date of birth known? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10020
        scenario.next();
        scenario.answer("yes");
        // (Id10021) When was the deceased born? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10021
        scenario.next();
        scenario.answer(LocalDate.parse("1998-01-01"));
        // (Id10022) Is the date of death known? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10022
        scenario.next();
        scenario.answer("yes");
        // (Id10021) When was the deceased born? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10021
        scenario.next();
        scenario.answer(LocalDate.parse("2018-01-01"));

        /*
         * Regression happens here: we changed FormInstanceParser to add all descendants of a group as targets
         * of a relevance condition defined in that group.
         *
         * When a field inside the group has also a relevance condition, then we have two equipotent condition
         * triggerables in the DAG that will update the field's relevance, which may set it in unexpected ways.
         * In this form, the Id10120_0 should be irrelevant at this point, but some relevance expression
         * declared in an ancestor group is making it relevant.
         *
         * In v2.17.0, we compute the descendant targets just in time, which makes the condition triggerables
         * not equipotent, ensuring that the one declared in the field will be evaluated last, producing the
         * expected relevance state.
         */
        assertThat(scenario.getAnswerNode("/data/consented/illhistory/illdur/Id10120_0"), is(irrelevant()));
    }

}
