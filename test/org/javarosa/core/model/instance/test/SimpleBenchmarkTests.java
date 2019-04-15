package org.javarosa.core.model.instance.test;



import org.javarosa.core.PathConst;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.ReferenceManagerTest;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.javarosa.core.reference.ReferenceManagerTestUtils.buildReferenceFactory;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SimpleBenchmarkTests {

//    @Test
//    public void testPopulate_withNodesAttributes() {
//        // Given
//        FormParseInit formParseInit = new FormParseInit(r("nigeria_wards_external_combined.xml"));
//
//        FormEntryController formEntryController = formParseInit.getFormEntryController();
//
//        byte[] formInstanceAsBytes = null;
//        try {
//            formInstanceAsBytes =
//                    Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(),
//                            "nigeria_wards_external_combined.xml"));
//        } catch (IOException e) {
//            fail("There was a problem with reading the test data.\n" + e.getMessage());
//        }
//        TreeElement savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
//        FormDef formDef = formEntryController.getModel().getForm();
//        TreeElement dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
//
//        // When
//        dataRootNode.populate(savedRoot, formDef);
//
//        // Then
//        assertEquals("Nigeria Wards External", formDef.getName());
//        assertEquals(3, Collections.list(formDef.getNonMainInstances()).size());
//        assertEquals(8, dataRootNode.getNumChildren());
//        TreeElement stateSelect1Question = dataRootNode.getChildAt(0);
//        TreeElement lgaSelect1Question = dataRootNode.getChildAt(1);
//        TreeElement wardSelect1Question = dataRootNode.getChildAt(2);
//        TreeElement commentsText1Question = dataRootNode.getChildAt(3);
//        TreeElement popFilterText1Question = dataRootNode.getChildAt(5);
//        TreeElement targetPopFilterText1Question = dataRootNode.getChildAt(4);
//
//
//
//        assertEquals("state", stateSelect1Question.getName());
//        assertEquals("lga", lgaSelect1Question.getName());
//        assertEquals("ward", wardSelect1Question.getName());
//        assertEquals("comments", commentsText1Question.getName());
//        assertEquals("pop_filter", popFilterText1Question.getName());
//        assertEquals("target_pop", targetPopFilterText1Question.getName());
//
//    }
//
//
//
//    @Test
//    public void testParseXFormXMLWithOnlyInternalDataInstances() {
//        //Path xFormFilePath = r("nigeria_wards_external_combined.xml");
//        Document xFormDocumentInMemory;
//
//
//        try {
//            byte[] xFormInstanceAsBytes =
//                Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(),
//                    "nigeria_wards_external_combined.xml"));
//            InputStreamReader xFormInputStreamReader = new InputStreamReader(new ByteArrayInputStream(xFormInstanceAsBytes));
//            xFormDocumentInMemory = XFormParser.getXMLDocument(xFormInputStreamReader, null);
//        } catch (IOException e) {
//            fail("There was a problem with reading the test data.\n" + e.getMessage());
//            throw new RuntimeException(e);
//        }
//
//        assertEquals( "html", xFormDocumentInMemory.getRootElement().getName());
//
//
//    }
//
//    @Test
//    public void testParseXFormXMLWithExternalDataInstances() {
//        Document xFormDocumentInMemory;
//
//
//        try {
//            byte[] xFormInstanceAsBytes =
//                Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(),
//                    "nigeria_wards_external.xml"));
//            InputStreamReader xFormInputStreamReader = new InputStreamReader(new ByteArrayInputStream(xFormInstanceAsBytes));
//            xFormDocumentInMemory = XFormParser.getXMLDocument(xFormInputStreamReader, null);
//            xFormDocumentInMemory.getChildCount();
//        } catch (IOException e) {
//            fail("There was a problem with reading the test data.\n" + e.getMessage());
//            throw new RuntimeException(e);
//        }
//
//        assertEquals( "html", xFormDocumentInMemory.getRootElement().getName());
//
//
//    }

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
        // assertEquals(11800, formDef.getNonMainInstance("wards").getRoot().getNumChildren());
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





}
