/*
 * Copyright 2019 Nafundi
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
import static org.javarosa.form.api.FormEntryController.EVENT_BEGINNING_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_END_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_PROMPT_NEW_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT_JUNCTURE;

import java.nio.file.Path;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

public class NigeriaWardsBrowsingRunBenchmark {
    public static void main(String[] args) {
        dryRun(NigeriaWardsBrowsingRunBenchmark.class);
    }

    @State(Scope.Thread)
    public static class NigeriaWardsState {
        private FormEntryController fec;

        @Setup(Level.Invocation)
        public void setUp() {
            Path assetsPath = prepareAssets("nigeria_wards_internal.xml");
            Path formFile = assetsPath.resolve("nigeria_wards_internal.xml");
            FormParseInit formParseInit = new FormParseInit(formFile);
            fec = formParseInit.getFormEntryController();
            Localizer l = formParseInit.getFormDef().getLocalizer();
            l.setDefaultLocale("en");
            l.setLocale("en");
            fec.stepToNextEvent();
        }

        @TearDown(Level.Invocation)
        public void tearDown() {
            fec = null;
        }
    }

    @Benchmark
    public void select_a_state_lga_and_ward_browse_back_to_beginning_and_go_through_to_the_end_of_the_form(NigeriaWardsState state, Blackhole bh) {
        // Answer the first question
        bh.consume(state.fec.answerQuestion(new StringData("7b0ded95031647702b8bed17dce7698a"), true)); // Abia

        // Step to the next question and force a call to populateDynamicChoices()
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.getModel().getQuestionPrompt().getSelectChoices());

        // Answer the second question
        bh.consume(state.fec.answerQuestion(new StringData("6fa741c46485b9c618f14b79edf50e88"), true)); // Aba North

        // Step to the next question and force a call to populateDynamicChoices()
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.getModel().getQuestionPrompt().getSelectChoices());

        // Answer the third question
        bh.consume(state.fec.answerQuestion(new StringData("90fa443787485709a5b11c5f7925fb71"), true)); // Ariaria

        // Step back and force a call to populateDynamicChoices()
        bh.consume(state.fec.stepToPreviousEvent());
        bh.consume(state.fec.getModel().getQuestionPrompt().getSelectChoices());

        // Step back and force a call to populateDynamicChoices()
        bh.consume(state.fec.stepToPreviousEvent());
        bh.consume(state.fec.getModel().getQuestionPrompt().getSelectChoices());

        // Step to the next question and force a call to populateDynamicChoices()
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.getModel().getQuestionPrompt().getSelectChoices());

        // Step to the next question and force a call to populateDynamicChoices()
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.getModel().getQuestionPrompt().getSelectChoices());

        // Step through the rest of questions to the form's end
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.stepToNextEvent());
        bh.consume(state.fec.stepToNextEvent());
    }

    private static String decodeJumpResult(int code) {
        switch (code) {
            case EVENT_BEGINNING_OF_FORM:
                return "Beginning of Form";
            case EVENT_END_OF_FORM:
                return "End of Form";
            case EVENT_PROMPT_NEW_REPEAT:
                return "Prompt new Repeat";
            case EVENT_QUESTION:
                return "Question";
            case EVENT_GROUP:
                return "Group";
            case EVENT_REPEAT:
                return "Repeat";
            case EVENT_REPEAT_JUNCTURE:
                return "Repeat Juncture";
        }
        return "Unknown";
    }
}