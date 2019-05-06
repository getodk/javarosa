package org.javarosa.benchmarks.utils;


import org.javarosa.benchmarks.BenchmarkUtils;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.javarosa.benchmarks.BenchmarkUtils.prepareAssets;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.buildReferenceFactory;

public class SimpleBenchmarkTests {

    @Test
    public void testParseXFormWithoutExternalInstance() {
        Path xFormFilePath = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
        FormDef formDef;
        try {
        formDef  = FormParserHelper.parse(xFormFilePath);
      } catch (IOException e) {
          fail("There was a problem with reading the test data.\n" + e.getMessage());
          throw new RuntimeException(e);
      }

        assertEquals( "Nigeria Wards", formDef.getName());
        assertEquals(3, Collections.list(formDef.getNonMainInstances()).size());
        assertNotNull(formDef.getNonMainInstance("lgas"));
        assertNotNull(formDef.getNonMainInstance("wards"));
        assertNotNull(formDef.getNonMainInstance("states"));
        assertEquals(774, formDef.getNonMainInstance("lgas").getRoot().getNumChildren());
        assertEquals(37, formDef.getNonMainInstance("states").getRoot().getNumChildren());
    }


    @Test
    public void testParseXFormWithExternalInstance() {
        Path resourcePath = prepareAssets();
        ReferenceManager.instance().addReferenceFactory((buildReferenceFactory("file", resourcePath.toString())));

        Path xFormFilePath = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance();
        FormDef formDef;
        try {
            formDef  = FormParserHelper.parse(xFormFilePath);
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
            throw new RuntimeException(e);
        }
        ArrayList<DataInstance> secondaryInstances = Collections.list(formDef.getNonMainInstances());

        assertEquals( "Nigeria Wards", formDef.getName());
        assertEquals(3, secondaryInstances.size());

        // assertEquals("Nigeria Wards External",formDef.getMainInstance().getName());
        assertNotNull(formDef.getNonMainInstance("lgas"));
        assertNotNull(formDef.getNonMainInstance("wards"));
        assertNotNull(formDef.getNonMainInstance("states"));
        assertEquals(774, formDef.getNonMainInstance("lgas").getRoot().getNumChildren());
        assertEquals(11800, formDef.getNonMainInstance("wards").getRoot().getNumChildren());
        assertEquals(37, formDef.getNonMainInstance("states").getRoot().getNumChildren());
    }


    @Test
    public void testParseExternalInstance() {
        Path resourcePath = prepareAssets();
        ReferenceManager.instance().addReferenceFactory((buildReferenceFactory("file", resourcePath.toString())));

        ExternalDataInstance externalDataInstance = null;
        try {
            externalDataInstance  =  ExternalDataInstance.build("jr://file/wards.xml", "wards");
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (InvalidReferenceException e) {
            e.printStackTrace();
        } catch (UnfullfilledRequirementsException e) {
            e.printStackTrace();
        } catch (InvalidStructureException e) {
            e.printStackTrace();
        }

        assertEquals( "wards", externalDataInstance.getName());
        assertNotNull(externalDataInstance);
        assertEquals(11800, externalDataInstance.getRoot().getNumChildren());
    }

    @Test
    public void testFormEntryControllerValidate() throws IOException {
        Path xFormFilePath = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance();
        ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", prepareAssets());
        FormDef formDef = FormParserHelper.parse(xFormFilePath);
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);
        HashMap<FormIndex, IAnswerData> answersMap = new HashMap<>();

        formEntryController.stepToNextEvent();
        while(formEntryModel.getFormIndex().isInForm()){
            FormIndex questionIndex = formEntryController.getModel().getFormIndex();
            FormEntryPrompt formEntryPrompt = formEntryModel.getQuestionPrompt(questionIndex);

            //Resolve DynamicChoices
            QuestionDef question = formEntryModel.getQuestionPrompt(questionIndex).getQuestion();
            ItemsetBinding itemsetBinding = question.getDynamicChoices();

            if(itemsetBinding != null){
                formDef.populateDynamicChoices(itemsetBinding, (TreeReference) question.getBind().getReference());
            }

            IAnswerData answer = null;
            answersMap.put(questionIndex, answer);
            formEntryController.answerQuestion(questionIndex, answer, true);

            switch (question.getLabelInnerText()){
                case "State":
                    answer = new SelectOneData(new Selection(question.getChoices().get(0)));
                    break;
                case "LGA":
                    answer = new SelectOneData(new Selection(question.getDynamicChoices().getChoices().get(0)));
                    break;
                case "Ward":
                    answer = new SelectOneData(new Selection(question.getDynamicChoices().getChoices().get(0)));
                    break;
                case "Comments":
                    answer = new StringData("No Comment");
                    break;
                case "What population do you want to search for?":
                    answer = new LongData(699967);
                    break;
                default:
                    answer = new LongData(0);

            }
            formEntryController.saveAnswer(questionIndex, answer, true);
            formEntryController.stepToNextEvent();
        }
        assertNotNull(answersMap);

    }

    @Test
    public void ParseXMLDocument() throws  IOException {
        Path xFormFilePath = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
        Reader reader = new FileReader(xFormFilePath.toFile());
        Document document = XFormParser.getXMLDocument(reader);
        assertNotNull(document);
    }

    @Test
    public void FormDefWriteAndReadFromToCache() throws  IOException{
        initSerialization();

        Path resourcePath = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
        //Setup reference manager
        ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", prepareAssets());
        //Parse File to FormDef
        FormDef formDef = FormParserHelper.parse(resourcePath);
        //Save to cache
        FormDefCache.writeCache(formDef, resourcePath.toAbsolutePath().toString());
        //Read from cache
        FormDef cachedFormDef = FormDefCache.readCache(resourcePath.toFile());
        //Run assertions
        assertNotNull(cachedFormDef);
        assertEquals(formDef.getName(), cachedFormDef.getName());
        assertEquals(formDef.getTitle(), cachedFormDef.getTitle());
        assertEquals(formDef.getMainInstance().getName(), cachedFormDef.getMainInstance().getName());
        assertEquals(Arrays.asList(formDef.getNonMainInstances()).size(),
            Arrays.asList(cachedFormDef.getNonMainInstances()).size());
    }

    private void initSerialization() {
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();
    }
}
