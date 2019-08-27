package org.javarosa.benchmarks.static_forms;

import org.javarosa.benchmarks.BenchmarkUtils;
import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryController;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;

public class StaticFormAnswerQ3Benchmark {
    public static void main(String[] args) {
        dryRun(StaticFormAnswerQ3Benchmark.class);
    }

    @State(Scope.Thread)
    public static class FormControllerAnswerQuestionState {
        FormEntryController formEntryController;

        @Setup(Level.Trial)
        public void initialize() throws IOException {
            formEntryController = BenchmarkUtils.getFormEntryController(BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance());
            formEntryController.stepToNextEvent();
            formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
            BenchmarkUtils.answerNextQuestion(formEntryController, false);
            BenchmarkUtils.answerNextQuestion(formEntryController, false);
        }
    }

    @Benchmark
    public void benchmarkAnswerQ1(FormControllerAnswerQuestionState state) {
        state.formEntryController.stepToNextEvent();
        BenchmarkUtils.answerNextQuestion(state.formEntryController, false);
    }

    @Benchmark
    public void benchmarkAnswerSaveQ1(FormControllerAnswerQuestionState state) {
        state.formEntryController.stepToNextEvent();
        BenchmarkUtils.answerNextQuestion(state.formEntryController, true);
    }

}
