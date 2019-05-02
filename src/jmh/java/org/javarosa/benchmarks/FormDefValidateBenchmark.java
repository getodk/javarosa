package org.javarosa.benchmarks;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance;

public class FormDefValidateBenchmark {
    public static void main(String[] args) {
        dryRun(FormDefValidateBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormDefValidateState {
        FormDef formDef;

        @Setup(Level.Trial)
        public void initialize() throws IOException {
            Path resourcePath = getNigeriaWardsXMLWithExternal2ndryInstance();
            formDef = FormParserHelper.parse(resourcePath);
            FormEntryModel formEntryModel = new FormEntryModel(formDef);
            FormEntryController formEntryController = new FormEntryController(formEntryModel);
            formEntryController.stepToNextEvent();
            while (formEntryModel.getFormIndex().isInForm()) {
                FormIndex questionIndex = formEntryController.getModel().getFormIndex();
                QuestionDef question = formEntryModel.getQuestionPrompt(questionIndex).getQuestion();
                FormEntryPrompt formEntryPrompt = formEntryModel.getQuestionPrompt(questionIndex);
                //Resolve Dynamic Choices
                ItemsetBinding itemsetBinding = question.getDynamicChoices();
                if (itemsetBinding != null) {
                    formDef.populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
                }
                IAnswerData answer = BenchmarkUtils.answerNigeriaWardsQuestion(formEntryPrompt.getQuestion());
                formEntryController.answerQuestion(questionIndex, answer, true);
                formEntryController.stepToNextEvent();
            }
            formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
        }
    }

    //@Benchmark
    public void benchmark_FormDefValidate_validate(FormDefValidateState state, Blackhole bh) {
        bh.consume(state.formDef.validate(true));
    }

}
