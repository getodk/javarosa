package org.javarosa.xml;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Before;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

public class TreeElementParserTest {

    private static Path SECONDARY_INSTANCE_XML;

    @Before
    public void setUp() {
        SECONDARY_INSTANCE_XML = r("secondary-instance.xml");
    }

    @Test
    public void parseInternalInstances() throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {

        InputStream inputStream = new FileInputStream(SECONDARY_INSTANCE_XML.toString());
        KXmlParser kXmlParser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser = new TreeElementParser(kXmlParser, 0, "");
        List<TreeElement> treeElementList = treeElementParser.parseInternalSecondaryInstances();
        assertEquals(treeElementList.size(), 1);
        TreeElement townsTreeElement = treeElementList.get(0);
        assertEquals(townsTreeElement.getInstanceName(), "towns");
        assertEquals(townsTreeElement.getNumChildren(), 1); //Has only one root node - <towndata z="1">
        assertEquals(townsTreeElement.getChildAt(0).getNumChildren(), 1); //Has only one data - <data_set>
        assertEquals(townsTreeElement.getChildAt(0)
            .getChildAt(0) //<data_set>us_east</data_set>
            .getValue().getDisplayText(), "us_east"); //Text Node - us_east

    }


    @Test
    public void parseInternalInstance() throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {

        InputStream inputStream = new FileInputStream(SECONDARY_INSTANCE_XML.toString());
        KXmlParser kXmlParser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser = new TreeElementParser(kXmlParser, 0, "towns");
        TreeElement townsTreeElement = treeElementParser.parseInternalSecondaryInstance();
        assertEquals("towns", townsTreeElement.getInstanceName());
        assertEquals(townsTreeElement.getNumChildren(), 1); //Has only one root node - <towndata z="1">
        assertEquals(townsTreeElement.getChildAt(0).getNumChildren(), 1); //Has only one data - <data_set>
        assertEquals(townsTreeElement.getChildAt(0)
            .getChildAt(0) //<data_set>us_east</data_set>
            .getValue().getDisplayText(), "us_east"); //Text Node - us_east

    }

}