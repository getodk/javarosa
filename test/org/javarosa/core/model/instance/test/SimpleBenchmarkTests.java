package org.javarosa.core.model.instance.test;



import org.javarosa.core.util.PathConst;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

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





}
