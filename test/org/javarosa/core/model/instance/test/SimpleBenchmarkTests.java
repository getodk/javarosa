package org.javarosa.core.model.instance.test;



import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
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
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;


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
            IAnswerData answer = getStubAnswer(formEntryPrompt.getQuestion());
            answersMap.put(questionIndex, answer);
            formEntryController.answerQuestion(questionIndex, answer, true);
            formEntryController.saveAnswer(questionIndex, answer, true);
            formEntryController.stepToNextEvent();
        }

        assertNotNull(answersMap);

    }


    private IAnswerData getStubAnswer(QuestionDef questionDef) {
        switch (questionDef.getControlType()){
            case Constants.CONTROL_INPUT:
                return new LongData(2);
//            case Constants.CONTROL_SELECT_ONE:
//                Selection selection = new Selection(1);
//                selection.attachChoice(questionDef);
//                return new SelectOneData(selection);
                default:
                    return  new IAnswerData() {
                        @Override
                        public void setValue(Object o) {}

                        @Override
                        public Object getValue() {
                            return "Auto Generated Answer";
                        }

                        @Override
                        public String getDisplayText() {
                            return getValue().toString();
                        }

                        @Override
                        public IAnswerData clone() {
                            return this;
                        }

                        @Override
                        public UncastData uncast() {
                            return null;
                        }

                        @Override
                        public IAnswerData cast(UncastData data) throws IllegalArgumentException {
                            return null;
                        }

                        @Override
                        public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {

                        }

                        @Override
                        public void writeExternal(DataOutputStream out) throws IOException {
                            out.writeBytes(getValue().toString());
                        }
                    };


        }
    }


}
