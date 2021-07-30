package org.javarosa.benchmarks.utils;

import org.javarosa.benchmarks.utils.builder.XFormBuilder;
import org.javarosa.benchmarks.utils.builder.XFormXmlDef;
import org.javarosa.benchmarks.utils.builder.form.Choice;
import org.javarosa.benchmarks.utils.builder.form.ChoiceSelector;
import org.javarosa.benchmarks.utils.builder.form.Question;
import org.javarosa.benchmarks.utils.builder.form.QuestionGroup;
import org.javarosa.benchmarks.utils.builder.form.QuestionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used to create the XForm Builder and to generate the Xform XML File
 * into the specified working directory
 */
public class XFormFileGenerator {

    public File generateXFormFile(String title, int noOfQuestions, int noOfQuestionGroups, int noOfInternalSecondaryInstances, int noOfExternalSecondaryInstances, int noOf2ndryInstanceElements, Path workingDirectory) throws IOException {
        File file = new File(workingDirectory.resolve(title + ".xml").toString());
        FileWriter fileWriter = new FileWriter(file);
        XFormBuilder xFormBuilder = generateXFormBuilder(title, noOfQuestions, noOfQuestionGroups, noOfInternalSecondaryInstances, noOfExternalSecondaryInstances, noOf2ndryInstanceElements,workingDirectory);
        fileWriter.write(xFormBuilder.build());
        fileWriter.close();
        return file;
    }

    public XFormBuilder generateXFormBuilder(String title, int noOfQuestions, int noOfQuestionGroups, int noOfInternalSecondaryInstances, int noOfExternalSecondaryInstances, int noOf2ndryInstanceElements, Path workingDirectory) throws IOException {
        List<ChoiceSelector> internalSecondaryInstances = generateOptionSelectors(ChoiceSelector.Type.INTERNAL, noOfInternalSecondaryInstances, noOf2ndryInstanceElements);
        List<ChoiceSelector> externalSecondaryInstances = generateOptionSelectors( ChoiceSelector.Type.EXTERNAL, noOfExternalSecondaryInstances, noOf2ndryInstanceElements);
        List<ChoiceSelector> secondaryInstances = new ArrayList<>();
        secondaryInstances.addAll(internalSecondaryInstances);
        secondaryInstances.addAll(externalSecondaryInstances);
        XFormXmlDef XFormXmlDef =
            new XFormXmlDef(
                title,
                generateQuestionGroups(noOfQuestionGroups, secondaryInstances),
                generateQuestions(noOfQuestions, secondaryInstances),
                internalSecondaryInstances,
                externalSecondaryInstances);

        XFormBuilder xFormFileBuilder = new XFormBuilder(XFormXmlDef, workingDirectory);
        return xFormFileBuilder;
    }

    private List<Question> generateQuestions(int noOfQuestions, List<ChoiceSelector> choiceSelectorList){
        List<Question> questions = new ArrayList<>(noOfQuestions);
        while(noOfQuestions > 0){
            questions.add(0, autoGenerate(noOfQuestions, choiceSelectorList));
            noOfQuestions--;
        }
        return questions;
    }

    private Question autoGenerate(int index, List<ChoiceSelector> choiceSelectorList) {
        Random random = new Random();
        Question question;
        ChoiceSelector choiceSelector = null;
        if(!choiceSelectorList.isEmpty()){
            int randomOptionSelector;
            randomOptionSelector = random.nextInt(choiceSelectorList.size());
            choiceSelector = choiceSelectorList.get(randomOptionSelector);
        }

        question = new Question(QuestionType.TEXT,"enter_input_" + index,"What is answer to question" + index + "?", "Hint to question " + index, choiceSelector);

        return question;
    }

    private List<QuestionGroup> generateQuestionGroups(int count, List<ChoiceSelector> choiceSelectorList){
        List<QuestionGroup> questionGroups = new ArrayList<>(count);
        while(count > 0){
            questionGroups.add(0, new QuestionGroup("group"+ count, generateQuestions(4, choiceSelectorList)));
            count--;
        }
        return questionGroups;
    }

    private List<ChoiceSelector> generateOptionSelectors(ChoiceSelector.Type type, int noOfOptionSelectors, int noOf2ndryInstanceElements){
        List<ChoiceSelector> instances = new ArrayList<>(noOfOptionSelectors);
        String secondaryInstanceType = type.toString().toLowerCase();
        String instanceIdTemplate = secondaryInstanceType +"_secondary_instance_%sE_%0" + (noOfOptionSelectors + "").length() +"d";
        while(noOfOptionSelectors > 0){
            instances.add(0, generateOptionSelector(String.format(instanceIdTemplate, noOf2ndryInstanceElements, noOfOptionSelectors), noOf2ndryInstanceElements));
            noOfOptionSelectors--;
        }
        return instances;
    }

    private ChoiceSelector generateOptionSelector(String instanceId, int noOfOptions){
        return new ChoiceSelector(instanceId, generateOptions(noOfOptions));
    }

    private List<Choice> generateOptions(int noOfOptions){
        List<Choice> choices = new ArrayList<>();
        for(int i = 0; i < noOfOptions; i++){
            choices.add(new Choice("item " + (i + 1), "option" + (i + 1)));
        }
        return choices;
    }

}
