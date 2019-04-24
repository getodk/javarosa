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
import java.util.ArrayList;
import java.util.HashMap;

import static org.javarosa.test.utils.ResourcePathHelper.r;

public class FormEntryControllerAnswerQuestion {

    @Test
    public void
    launchBenchmark() throws Exception {
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

        }
    }

    @State(Scope.Thread)
    public static class FormControllerAnswerQuestionState {
        FormEntryController formEntryController;
        FormEntryModel formEntryModel;
        @Setup(Level.Trial)
        public void
        initialize() throws IOException {
            Path resourcePath = r("nigeria_wards_external.xml");
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", PathConst.getTestResourcePath().toPath());
            FormDef formDef = FormParserHelper.parse(resourcePath);
            formEntryModel = new FormEntryModel(formDef);
            HashMap<FormIndex, IAnswerData> answersMap = new HashMap<>();
            formEntryController = new FormEntryController(formEntryModel);

            formEntryController.stepToNextEvent();
            while(formEntryModel.getFormIndex().isInForm()) {
                FormIndex questionIndex = formEntryController.getModel().getFormIndex();
                QuestionDef question = formEntryModel.getQuestionPrompt(questionIndex).getQuestion();
                //Resolve DynamicChoices
                ItemsetBinding itemsetBinding = question.getDynamicChoices();
                if(itemsetBinding != null){
                    formDef.populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
                }
                formEntryController.stepToNextEvent();
            }
            formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
        }
    }

    @Benchmark
    public void
    benchmark_FormEntryController_answerAndSaveAll(FormControllerAnswerQuestionState state, Blackhole bh) {
        state.formEntryController.stepToNextEvent();
        while(state.formEntryModel.getFormIndex().isInForm()){
            FormIndex questionIndex = state.formEntryController.getModel().getFormIndex();
            FormEntryPrompt formEntryPrompt = state.formEntryModel.getQuestionPrompt(questionIndex);
            QuestionDef question = formEntryPrompt.getQuestion();
            ItemsetBinding itemsetBinding = question.getDynamicChoices();
            if(itemsetBinding != null){
                state.formEntryController.getModel().getForm()
                    .populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
            }
            IAnswerData answer = BenchmarkUtils.getStubAnswer(formEntryPrompt.getQuestion());
            state.formEntryController.answerQuestion(questionIndex, answer, true);
            state.formEntryController.saveAnswer(questionIndex, answer, true);
            state.formEntryController.stepToNextEvent();
        }
        state.formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
    }

    @Benchmark
    public void
    benchmark_FormEntryController_answerAll(FormControllerAnswerQuestionState state, Blackhole bh) {
        state.formEntryController.stepToNextEvent();
        while(state.formEntryModel.getFormIndex().isInForm()){
            FormIndex questionIndex = state.formEntryController.getModel().getFormIndex();
            FormEntryPrompt formEntryPrompt = state.formEntryModel.getQuestionPrompt(questionIndex);
            QuestionDef question = formEntryPrompt.getQuestion();
            ItemsetBinding itemsetBinding = question.getDynamicChoices();
            if(itemsetBinding != null){
                state.formEntryController.getModel().getForm()
                    .populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
            }
            IAnswerData answer = BenchmarkUtils.getStubAnswer(formEntryPrompt.getQuestion());
            state.formEntryController.answerQuestion(questionIndex, answer, true);
            state.formEntryController.stepToNextEvent();
        }
        state.formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
    }

    @Benchmark
    public void
    benchmark_FormEntryController_answerAllThenSaveAll(FormControllerAnswerQuestionState state, Blackhole bh) {
        ArrayList failures = new ArrayList();
        HashMap<FormIndex, IAnswerData> answers = new HashMap<>();
        state.formEntryController.stepToNextEvent();
        while(state.formEntryModel.getFormIndex().isInForm()){
            FormIndex questionIndex = state.formEntryController.getModel().getFormIndex();
            FormEntryPrompt formEntryPrompt = state.formEntryModel.getQuestionPrompt(questionIndex);
            QuestionDef question = formEntryPrompt.getQuestion();
            ItemsetBinding itemsetBinding = question.getDynamicChoices();
            if(itemsetBinding != null){
                state.formEntryController.getModel().getForm()
                    .populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
            }
            IAnswerData answer = BenchmarkUtils.getStubAnswer(formEntryPrompt.getQuestion());
            int saveStatus = state.formEntryController.answerQuestion(questionIndex, answer, true);
            if(saveStatus != FormEntryController.ANSWER_OK){ failures.add(failures); }
            answers.put(questionIndex, answer);
            state.formEntryController.stepToNextEvent();
        }
        for(FormIndex questionIndex: answers.keySet()){
            state.formEntryController.saveAnswer(questionIndex, answers.get(questionIndex),false);
        }
        state.formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
    }


    @Benchmark
    public void
    benchmark_FormEntryController_answerOne(FormControllerAnswerQuestionState state, Blackhole bh) throws RuntimeException {
        state.formEntryController.stepToNextEvent();
        if(state.formEntryModel.getFormIndex().isInForm()){
            FormIndex questionIndex = state.formEntryController.getModel().getFormIndex();
            FormEntryPrompt formEntryPrompt = state.formEntryModel.getQuestionPrompt(questionIndex);
            IAnswerData answer = BenchmarkUtils.getStubAnswer(formEntryPrompt.getQuestion());
            state.formEntryController.answerQuestion(questionIndex, answer, true);
            state.formEntryController.stepToNextEvent();
        }else{
            throw new RuntimeException("Form controller not in a question index");
        }
        state.formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
    }
}
