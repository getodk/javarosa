package org.javarosa.core.benchmark;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.PathConst;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.fail;


public class BenchmarkUtils {

    public static Object benchMark2(){
        // Given
        FormParseInit formParseInit = new FormParseInit(r("populate-nodes-attributes.xml"));

        FormEntryController formEntryController = formParseInit.getFormEntryController();

        byte[] formInstanceAsBytes = null;
        try {
            formInstanceAsBytes =
                Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(),
                    "populate-nodes-attributes-instance.xml"));
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
        }
        TreeElement savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
        FormDef formDef = formEntryController.getModel().getForm();
        TreeElement dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
        return dataRootNode;
    }

    public static Options getJVMOptions(String className){
        Options opt = new OptionsBuilder()
            // Specify which benchmarks to run.
            // You can be more specific if you'd like to run only one benchmark per test.
            .include(className + ".*")
            // Set the following options as needed
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.SECONDS)
            .warmupTime(TimeValue.seconds(2))
            .warmupIterations(20)
            .measurementTime(TimeValue.seconds(2))
            .threads(1)
            .measurementIterations(10)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
            //.jvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=10")
            //.addProfiler(WinPerfAsmProfiler.class)
            .build();

        return opt;
    }


    public static IAnswerData answerNigeriaWardsQuestion(QuestionDef question) {
        IAnswerData answer;
        switch (question.getLabelInnerText()){
            case "State":
                answer = new SelectOneData(new Selection(question.getChoices().get(0)));//Abia
                break;
            case "LGA":
                answer = new SelectOneData(new Selection(question.getDynamicChoices().getChoices().get(0)));//Aba North
                break;
            case "Ward":
                answer = new SelectOneData(new Selection(question.getDynamicChoices().getChoices().get(0)));//Ariaria
                break;
            case "Comments":
                answer = new StringData("No Comment");
                break;
            case "What population do you want to search for?":
                answer = new LongData(699967);
                break;
            default:
                answer = new StringData("Question not understood...");
        }
        return answer;
    }

    public static File getNigeriaWardsXMLWithInternal2ndryInstance(){
        Path filePath = r("nigeria_wards_internal_2ndry_instance.xml");
        return filePath.toFile();
    }

    public static File getNigeriaWardsXMLWithExternal2ndryInstance(){
        Path filePath = r("nigeria_wards_external_2ndry_instance.xml");
        return filePath.toFile();
    }

}
