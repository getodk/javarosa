package org.javarosa.xml;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xml.util.InvalidStructureException;
import org.junit.Before;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TreeElementParserTest {

    private static Path SECONDARY_INSTANCE_XML;

    @Before
    public void setUp() {
        SECONDARY_INSTANCE_XML = r("secondary-instance.xml");
    }

    @Test
    public void parseInternalInstances() throws IOException, XmlPullParserException, InvalidStructureException {

        InputStream inputStream = new FileInputStream(SECONDARY_INSTANCE_XML.toString());
        KXmlParser kXmlParser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser = new TreeElementParser(kXmlParser, 0, "");
        List<TreeElement> treeElementList = treeElementParser.parseInternalSecondaryInstances();
        assertEquals(treeElementList.size(), 1);
        TreeElement townsTreeElement = treeElementList.get(0);
        assertEquals(townsTreeElement.getInstanceName(), "towns");
        assertEquals(townsTreeElement.getNumChildren(), 1); // Has only one root node - <towndata z="1">
        assertEquals(townsTreeElement.getChildAt(0).getNumChildren(), 1); // Has only one data - <data_set>
        assertEquals(townsTreeElement.getChildAt(0)
            .getChildAt(0) // <data_set>us_east</data_set>
            .getValue().getDisplayText(), "us_east"); // Text Node - us_east

    }

    @Test
    public void parseInternalSecondaryInstances_DoesNotIncludeExternalInstances() throws IOException, InvalidStructureException, XmlPullParserException {
        InputStream inputStream = new FileInputStream(r("external-select-xml.xml").toString());
        KXmlParser kXmlParser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser = new TreeElementParser(kXmlParser, 0, "");
        List<TreeElement> internalSecondaryInstances = treeElementParser.parseInternalSecondaryInstances();

        assertThat(internalSecondaryInstances, is(empty()));
    }

    @Test
    public void parseInternalSecondaryInstances_DoesNotIncludeThePrimaryInstance_WhenItHasAnId() throws IOException, InvalidStructureException, XmlPullParserException {
        InputStream inputStream = new FileInputStream(r("primary-instance-with-id.xml").toString());
        KXmlParser kXmlParser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser = new TreeElementParser(kXmlParser, 0, "");
        List<TreeElement> internalSecondaryInstances = treeElementParser.parseInternalSecondaryInstances();

        assertThat(internalSecondaryInstances.size(), is(1));
    }
}