package org.javarosa.xform.util.test;

import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.SecondaryInstanceAnalyzer;
import org.junit.Test;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class SecondaryInstanceAnalyzerTest {
    private static final Logger logger = LoggerFactory.getLogger(SecondaryInstanceAnalyzer.class);

    private void parseElement(Element element, SecondaryInstanceAnalyzer secondaryInstanceAnalyzer) {
        for (int i = 0; i < element.getChildCount(); i++) {
            if (element.getType(i) == Element.ELEMENT) {
                secondaryInstanceAnalyzer.analyzeElement(element);
                parseElement(element.getElement(i), secondaryInstanceAnalyzer);
            }
        }
    }

    @Test
    public void getIDFromAttributes() throws IOException {
        String filePath = r("secondary-instance-test.xml").toString();
        Document doc = XFormParser.getXMLDocument(new FileReader(filePath));
        Element root = doc.getRootElement();
        SecondaryInstanceAnalyzer secondaryInstanceAnalyzer = new SecondaryInstanceAnalyzer();
        parseElement(root, secondaryInstanceAnalyzer);

        assertEquals(secondaryInstanceAnalyzer.getInMemorySecondaryInstances().toArray().length, 2);
        assertFalse(secondaryInstanceAnalyzer.shouldSecondaryInstanceBeParsed("country"));
        assertTrue(secondaryInstanceAnalyzer.shouldSecondaryInstanceBeParsed("lgas"));
    }
}
