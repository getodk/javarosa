package org.javarosa.core.model.instance.test;



import org.javarosa.core.model.Constants;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.PathConst;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;


import static org.javarosa.core.reference.ReferenceManagerTestUtils.buildReferenceFactory;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SimpleBenchmarkTests {

    @Test
    public void testParseXFormWithoutExternalInstance() {
        Path xFormFilePath = r("nigeria_wards_external_combined.xml");
        FormDef formDef;
        try {
        formDef  = FormParserHelper.parse(xFormFilePath);
      } catch (IOException e) {
          fail("There was a problem with reading the test data.\n" + e.getMessage());
          throw new RuntimeException(e);
      }

        assertEquals( "Nigeria Wards External", formDef.getName());
        assertEquals(3, Collections.list(formDef.getNonMainInstances()).size());
        assertNotNull(formDef.getNonMainInstance("lgas"));
        assertNotNull(formDef.getNonMainInstance("wards"));
        assertNotNull(formDef.getNonMainInstance("states"));
        assertEquals(774, formDef.getNonMainInstance("lgas").getRoot().getNumChildren());
        assertEquals(37, formDef.getNonMainInstance("states").getRoot().getNumChildren());
    }


    @Test
    public void testParseXFormWithExternalInstance() {
        Path resourcePath = PathConst.getTestResourcePath().toPath();
        ReferenceManager.instance().addReferenceFactory((buildReferenceFactory("file", resourcePath.toString())));

        Path xFormFilePath = r("nigeria_wards_external.xml");
        FormDef formDef;
        try {
            formDef  = FormParserHelper.parse(xFormFilePath);
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
            throw new RuntimeException(e);
        }

        assertEquals( "Nigeria Wards External", formDef.getName());
        assertEquals(3, Collections.list(formDef.getNonMainInstances()).size());
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
        Path resourcePath = PathConst.getTestResourcePath().toPath();
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

        Path resourcePath = r("nigeria_wards_external.xml");
        ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", PathConst.getTestResourcePath().toPath());
        FormDef formDef = FormParserHelper.parse(resourcePath);
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
    public void FormDefWriteAndReadFromToCache() throws  IOException{

        // I am not sure if these commented sniipets have anything to do with the error
//        PropertyManager mgr = new PropertyManager();
//        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
//        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
//        new XFormsModule().registerModule();

//        // needed to override rms property manager
//        org.javarosa.core.services.PropertyManager
//            .setPropertyManager(mgr);

        Path resourcePath = r("nigeria_wards_external.xml");
        //Setup reference manager
        ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", PathConst.getTestResourcePath().toPath());
        //Parse File to FormDef
        FormDef formDef = FormParserHelper.parse(resourcePath);
        //Save to cache
        FormDefCache.writeCache(formDef, resourcePath.toAbsolutePath().toString());
        //Read from cache
        FormDef cachedFormDef = FormDefCache.readCache(resourcePath.toFile());
        //Run assertions
        assertNotNull(cachedFormDef);
    }

}
