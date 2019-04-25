package org.javarosa.core.benchmark;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.util.PathConst;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.FormParserHelper;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.NoBenchmarksException;
import org.openjdk.jmh.runner.Runner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import static org.javarosa.test.utils.ResourcePathHelper.r;

public class FormDefValidateBenchmark {

    @Test
    public void
    launchBenchmark() throws Exception {
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

        }
    }

    @State(Scope.Thread)
    public static class FormDefValidateState {
        FormDef formDef;
        @Setup(Level.Trial)
        public void
        initialize() throws IOException {
            Path resourcePath = r("nigeria_wards_external.xml");
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", PathConst.getTestResourcePath().toPath());
            formDef = FormParserHelper.parse(resourcePath);
            FormEntryModel formEntryModel = new FormEntryModel(formDef);
            FormEntryController formEntryController = new FormEntryController(formEntryModel);
            formEntryController.stepToNextEvent();
            while(formEntryModel.getFormIndex().isInForm()) {
                FormIndex questionIndex = formEntryController.getModel().getFormIndex();
                QuestionDef question = formEntryModel.getQuestionPrompt(questionIndex).getQuestion();
                FormEntryPrompt formEntryPrompt = formEntryModel.getQuestionPrompt(questionIndex);
                //Resolve Dynamic Choices
                ItemsetBinding itemsetBinding = question.getDynamicChoices();
                if(itemsetBinding != null){
                    formDef.populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
                }
                IAnswerData answer = BenchmarkUtils.getStubAnswer(formEntryPrompt.getQuestion());
                formEntryController.answerQuestion(questionIndex, answer, true);
                formEntryController.stepToNextEvent();
            }
            formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
        }
    }

    @Benchmark
    public void
    benchmark_FormDefValidate_validate(FormDefValidateState state, Blackhole bh) {
        bh.consume(state.formDef.validate(true));
    }

}
