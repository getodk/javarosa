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
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.test.QuestionDefMatchers.nonRelevant;
import static org.javarosa.core.test.Scenario.getRef;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.stream.IntStream;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

public class WhoVATest {
    @Test
    public void regression_after_2_17_0_relevance_updates() {
        Scenario scenario = Scenario.init(r("whova_form.xml"));

        // region Give consent to unblock the rest of the form
        // (Id10013) [Did the respondent give consent?] ref:/data/respondent_backgr/Id10013
        scenario.next(14);
        scenario.answer("yes");
        // endregion

        // region Info on deceased
        // (Id10019) What was the sex of the deceased? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10019
        scenario.next(6);
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
        // endregion

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
        assertThat(scenario.getAnswerNode("/data/consented/illhistory/illdur/Id10120_0"), is(nonRelevant()));
    }

    // TODO Add a comment pointing out which questions belong to the longest path or, at least, where it's exercised in this script.
    @Test
    public void smoke_test_route_fever_and_lumps() {
        Scenario scenario = Scenario.init(r("whova_form.xml"));

        // region Give consent to unblock the rest of the form
        // (Id10013) [Did the respondent give consent?] ref:/data/respondent_backgr/Id10013
        scenario.next(14);
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/respondent_backgr/Id10013")));
        scenario.answer("yes");
        // endregion

        // region Info on deceased
        // (Id10019) What was the sex of the deceased? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10019
        scenario.next(6);
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/deceased_CRVS/info_on_deceased/Id10019")));
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

        // Sanity check about age and isAdult field
        assertThat(scenario.answerOf("/data/consented/deceased_CRVS/info_on_deceased/ageInDays"), is(intAnswer(7305)));
        assertThat(scenario.answerOf("/data/consented/deceased_CRVS/info_on_deceased/isAdult"), is(stringAnswer("1")));
        assertThat(scenario.answerOf("/data/consented/deceased_CRVS/info_on_deceased/isNeonatal"), is(stringAnswer("0")));

        // Skip a bunch of non yes/no questions
        scenario.next(11);
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/illhistory/illdur/id10120_unit")));

        // Answer no to the rest of questions
        IntStream.range(0, 23).forEach(n -> {
            scenario.next();
            if (scenario.atQuestion())
                scenario.answer("no");
        });
        // endregion

        // region Signs and symptoms - fever
        // (Id10147) Did (s)he have a fever? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10147
        scenario.next();
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/illhistory/signs_symptoms_final_illness/Id10147")));
        scenario.answer("yes");
        // (Id10148_units) How long did the fever last? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10148_units
        scenario.next();
        scenario.answer("days");
        // (Id10148_b) [Enter how long the fever lasted in days]: ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10148_b
        scenario.next();
        scenario.answer(30);
        // (Id10149) Did the fever continue until death? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10149
        scenario.next();
        scenario.answer("yes");
        // (Id10150) How severe was the fever? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10150
        scenario.next();
        scenario.answer("severe");
        // (Id10151) What was the pattern of the fever? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10151
        scenario.next();
        scenario.answer("nightly");
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/illhistory/signs_symptoms_final_illness/Id10151")));
        // endregion

        // region Answer "no" until we get to the lumps group
        IntStream.range(0, 36).forEach(n -> {
            scenario.next();
            if (scenario.atQuestion())
                scenario.answer("no");
        });
        // endregion

        // region Signs and symptoms - lumps
        // (Id10253) Did (s)he have any lumps? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10253
        scenario.next();
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/illhistory/signs_symptoms_final_illness/Id10253")));
        scenario.answer("yes");
        // (Id10254) Did (s)he have any lumps or lesions in the mouth? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10254
        scenario.next();
        scenario.answer("yes");
        // (Id10255) Did (s)he have any lumps on the neck? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10255
        scenario.next();
        scenario.answer("yes");
        // (Id10256) Did (s)he have any lumps on the armpit? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10256
        scenario.next();
        scenario.answer("yes");
        // (Id10257) Did (s)he have any lumps on the groin? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10257
        scenario.next();
        scenario.answer("yes");
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/illhistory/signs_symptoms_final_illness/Id10257")));
        // endregion

        // region Answer "no" to almost the end of the form
        IntStream.range(0, 59).forEach(n -> {
            scenario.next();
            if (scenario.atQuestion())
                scenario.answer("no");
        });
        // endregion

        // region Answer the last question with comments
        scenario.next();
        assertThat(scenario.refAtIndex().genericize(), is(getRef("/data/consented/comment")));
        scenario.answer("No comments");

        scenario.next();
        assertThat(scenario.atTheEndOfForm(), is(true));
        // endregion
    }

}
