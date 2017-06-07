package org.javarosa.core.model.instance.test;


import org.javarosa.core.PathConst;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TreeElementTests {

    @Test
    public void testPopulate_withNodesAttributes() {
        // Given
        FormParseInit formParseInit = new FormParseInit();
        formParseInit.setFormToParse(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(),
                "populate-nodes-attributes.xml").toString());

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

        // When
        dataRootNode.populate(savedRoot, formDef);

        // Then
        assertEquals(2, dataRootNode.getNumChildren());
        TreeElement freeText1Question = dataRootNode.getChildAt(0);
        TreeElement regularGroup = dataRootNode.getChildAt(1);

        assertEquals(1, regularGroup.getNumChildren());
        TreeElement freeText2Question = regularGroup.getChildAt(0);

        assertEquals("free_text_1", freeText1Question.getName());
        assertEquals(1, freeText1Question.getAttributeCount());
        TreeElement customAttr1 = freeText1Question.getAttribute(null, "custom_attr_1");
        assertNotNull(customAttr1);
        assertEquals("custom_attr_1", customAttr1.getName());
        assertEquals("", customAttr1.getNamespace());
        assertEquals("xyz1", customAttr1.getAttributeValue());

        assertEquals("regular_group", regularGroup.getName());
        assertEquals(1, regularGroup.getAttributeCount());
        customAttr1 = regularGroup.getAttribute(null, "custom_attr_1");
        assertNotNull(customAttr1);
        assertEquals("custom_attr_1", customAttr1.getName());
        assertEquals("custom_name_space", customAttr1.getNamespace());
        assertEquals("xyz2", customAttr1.getAttributeValue());

        assertEquals("free_text_2", freeText2Question.getName());
        assertEquals(1, freeText1Question.getAttributeCount());
        TreeElement customAttr2 = freeText2Question.getAttribute(null, "custom_attr_2");
        assertNotNull(customAttr2);
        assertEquals("custom_attr_2", customAttr2.getName());
        assertEquals("", customAttr2.getNamespace());
        assertEquals("xyz3", customAttr2.getAttributeValue());
    }

}
