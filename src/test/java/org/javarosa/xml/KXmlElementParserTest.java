package org.javarosa.xml;

import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import static org.junit.Assert.assertEquals;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertNotNull;

public class KXmlElementParserTest {

    @Test
    public void testParseXForm() throws IOException, XmlPullParserException {
        Path xFormFilePath = r("simple-form.xml");
        Document document;
        KXmlElementParser kxmlElementParser ;

        InputStream inputStream = new FileInputStream(xFormFilePath.toString());
        KXmlParser kXmlParser = KXmlElementParser.instantiateParser(inputStream);
        kxmlElementParser = new KXmlElementParser(kXmlParser);
        document = kxmlElementParser.parseDoc();


        assertNotNull(document);
        Element htmlElement = document.getRootElement();
        //HTML
        assertEquals( "html", htmlElement.getName());
        assertEquals(2, htmlElement.getChildCount());
        //HEAD
        Element headElement = htmlElement.getElement(0);
        assertEquals( "head", headElement.getName());
        assertEquals("http://www.w3.org/1999/xhtml", headElement.getNamespace());
        //TITLE
        Element titleElement = headElement.getElement(0);
        assertEquals(2, headElement.getChildCount());
        assertEquals("Simple Form", titleElement.getText(0));
        //MODEL
        Element modelElement = headElement.getElement(1);
        assertEquals(3, modelElement.getChildCount());
        //MAIN INSTANCE
        Element mainInstanceElement = modelElement.getElement(0);
        assertEquals(null, mainInstanceElement.getAttributeValue(null, "id"));
        //FIRST SECONDARY INSTANCE
        Element firstSecondaryInstanceElement = modelElement.getElement(1);
        assertEquals(1, firstSecondaryInstanceElement.getChildCount());
        assertEquals("options1", firstSecondaryInstanceElement.getAttributeValue(null, "id"));
        Element rootElement = firstSecondaryInstanceElement.getElement(0);
        assertEquals("root", rootElement.getName());
        assertEquals(5, rootElement.getChildCount());
        //BIND ELEMENT
        Element bindElement = modelElement.getElement(2);
        assertEquals(4, bindElement.getAttributeCount());

    }


    @Test
    public void testParseXFormSkipInternalInstances() throws IOException, XmlPullParserException {
        NodeGatherer elementToSkip = new NodeGatherer("instance", 1);
        Path xFormFilePath = r("simple-form.xml");
        Document document;
        KXmlElementParser kxmlElementParser ;

        InputStream inputStream = new FileInputStream(xFormFilePath.toString());
        KXmlParser kXmlParser = KXmlElementParser.instantiateParser(inputStream);
        kxmlElementParser = new KXmlElementParser(kXmlParser, elementToSkip);
        document = kxmlElementParser.parseDoc();

        assertNotNull(document);
        Element htmlElement = document.getRootElement();
        //HTML
        assertEquals( "html", htmlElement.getName());
        assertEquals(2, htmlElement.getChildCount());
        //HEAD
        Element headElement = htmlElement.getElement(0);
        assertEquals( "head", headElement.getName());
        assertEquals("http://www.w3.org/1999/xhtml", headElement.getNamespace());
        //TITLE
        Element titleElement = headElement.getElement(0);
        assertEquals(2, headElement.getChildCount());
        assertEquals("Simple Form", titleElement.getText(0));
        //MODEL
        Element modelElement = headElement.getElement(1);
        assertEquals(3, modelElement.getChildCount());
        //MAIN INSTANCE
        Element mainInstanceElement = modelElement.getElement(0);
        assertEquals(null, mainInstanceElement.getAttributeValue(null, "id"));
        //FIRST SECONDARY INSTANCE
        Element firstSecondaryInstanceElement = modelElement.getElement(1);
        assertEquals("options1", firstSecondaryInstanceElement.getAttributeValue(null, "id"));
        assertEquals(0, firstSecondaryInstanceElement.getChildCount());
        //BIND ELEMENT
        Element bindElement = modelElement.getElement(2);
        assertEquals(4, bindElement.getAttributeCount());

    }

}
