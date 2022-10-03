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

package org.javarosa.benchmarks;


import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.prepareAssets;
import static org.javarosa.core.test.Scenario.init;

import java.time.LocalDate;
import java.util.stream.IntStream;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class WhoVaBenchmark {

    public static void main(String[] args) {
        dryRun(WhoVaBenchmark.class);
    }

    @State(Scope.Thread)
    public static class WhoVaState {
        public Scenario scenario;

        @Setup(Level.Trial)
        public void initialize() throws XFormParser.ParseException {
            scenario = init(prepareAssets("whova_form.xml").resolve("whova_form.xml"));
        }
    }

    @Benchmark
    public void run_1_times(WhoVaState state, Blackhole bh) {
        doRuns(state, bh, 1);
    }

    @Benchmark
    public void run_10_times(WhoVaState state, Blackhole bh) {
        doRuns(state, bh, 10);
    }

    @Benchmark
    public void run_20_times(WhoVaState state, Blackhole bh) {
        doRuns(state, bh, 20);
    }

    private void doRuns(WhoVaState state, Blackhole bh, int numberOfRuns) {
        for (int i = 0; i < numberOfRuns; i++) {
            state.scenario.newInstance();
            answerAllQuestions(state, bh);
        }
    }

    private void answerAllQuestions(WhoVaState state, Blackhole bh) {
        // region Give consent to unblock the rest of the form
        // (Id10013) [Did the respondent give consent?] ref:/data/respondent_backgr/Id10013
        state.scenario.next(14);
        state.scenario.answer("yes");
        // endregion

        // region Info on deceased
        // (Id10019) What was the sex of the deceased? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10019
        state.scenario.next(6);
        state.scenario.answer("female");
        // (Id10020) Is the date of birth known? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10020
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10021) When was the deceased born? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10021
        state.scenario.next();
        state.scenario.answer(LocalDate.parse("1998-01-01"));
        // (Id10022) Is the date of death known? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10022
        // This question triggers one of the longest evaluation chain of triggerables including 5 calculations
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10021) When was the deceased born? ref:/data/consented/deceased_CRVS/info_on_deceased/Id10021
        state.scenario.next();
        state.scenario.answer(LocalDate.parse("2018-01-01"));

        // Skip a bunch of non yes/no questions
        state.scenario.next(11);

        // Answer no to the rest of questions
        IntStream.range(0, 23).forEach(n -> {
            state.scenario.next();
            if (state.scenario.atQuestion())
                state.scenario.answer("no");
        });
        // endregion

        // region Signs and symptoms - fever
        // (Id10147) Did (s)he have a fever? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10147
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10148_units) How long did the fever last? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10148_units
        state.scenario.next();
        state.scenario.answer("days");
        // (Id10148_b) [Enter how long the fever lasted in days]: ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10148_b
        state.scenario.next();
        state.scenario.answer(30);
        // (Id10149) Did the fever continue until death? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10149
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10150) How severe was the fever? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10150
        state.scenario.next();
        state.scenario.answer("severe");
        // (Id10151) What was the pattern of the fever? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10151
        state.scenario.next();
        state.scenario.answer("nightly");
        // endregion

        // region Answer "no" until we get to the lumps group
        IntStream.range(0, 36).forEach(n -> {
            state.scenario.next();
            if (state.scenario.atQuestion())
                state.scenario.answer("no");
        });
        // endregion

        // region Signs and symptoms - lumps
        // (Id10253) Did (s)he have any lumps? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10253
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10254) Did (s)he have any lumps or lesions in the mouth? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10254
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10255) Did (s)he have any lumps on the neck? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10255
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10256) Did (s)he have any lumps on the armpit? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10256
        state.scenario.next();
        state.scenario.answer("yes");
        // (Id10257) Did (s)he have any lumps on the groin? ref:/data/consented/illhistory/signs_symptoms_final_illness/Id10257
        state.scenario.next();
        state.scenario.answer("yes");
        // endregion

        // region Answer "no" to almost the end of the form
        IntStream.range(0, 59).forEach(n -> {
            state.scenario.next();
            if (state.scenario.atQuestion())
                state.scenario.answer("no");
        });
        // endregion

        // region Answer the last question with comments
        state.scenario.next();
        state.scenario.answer("No comments");

        state.scenario.next();
        // endregion
    }

}
