package org.javarosa.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * <p>Element Parser is the core parsing element for XML files. Implementations
 * can be made for data types, encapsulating all of the parsing rules for that
 * type's XML definition.</p>
 *
 * <p>An Element parser should have a defined scope of a single tag and its
 * descendants in the document. The ElementParser should receive the parser
 * pointing to that opening tag, and return it on the closing tag
 *
 * <p>A number of helper methods are provided in the parser which are intended
 * to standardize the techniques used for validation and pull-parsing through
 * the XML Document.</p>
 *
 * @author ctsims
 */
public abstract class ElementParser<T> {
    private static final Logger logger = LoggerFactory.getLogger(ElementParser.class);

    protected final KXmlParser parser;

    /**
     * Produces a new element parser for the appropriate Element datatype.
     *
     * The parser should be already instantiated, and should be pointing directly
     * at the opening tag expected by the parser, not (for instance) the beginning
     * of the document
     *
     * @param parser An XML Pull Parser which is currently at the
     *               position of the top level element that represents this
     *               element's XML structure.
     */
    ElementParser(KXmlParser parser) {
        this.parser = parser;
    }

    /**
     * Prepares a parser that will be used by the element parser, configuring relevant
     * parameters and setting it to the appropriate point in the document.
     *
     * @param reader A reader reading the XML content
     *               of the document.
     * @throws IOException If the stream cannot be read for any reason
     *                     other than invalid XML Structures.
     */
    public static KXmlParser instantiateParser(Reader reader) throws IOException {
        KXmlParser parser = new KXmlParser();
        try {
            parser.setInput(reader);
            parser.setFeature(KXmlParser.FEATURE_PROCESS_NAMESPACES, true);

            //Point to the first available tag.
            parser.next();

            return parser;
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            logger.error("Element Parser", e);
            throw new IOException(e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Prepares a parser that will be used by the element parser, configuring relevant
     * parameters and setting it to the appropriate point in the document.
     *
     * @param stream A input stream which is reading the XML content
     *               of the document.
     * @throws IOException If the stream cannot be read for any reason
     *                     other than invalid XML Structures.
     */
    public static KXmlParser instantiateParser(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        return instantiateParser(reader);
    }

    /**
     * Parses the XML document at the current level, returning the datatype
     * described by the document.
     *
     * @return The datatype which is described by the appropriate XML
     * definition.
     * @throws InvalidStructureException If the XML does not contain properly
     *                                   structured XML
     * @throws IOException               If there is a problem retrieving the document
     * @throws XmlPullParserException    If the document does not contain well-
     *                                   formed XML.
     */
    public abstract T parse() throws InvalidStructureException, IOException, XmlPullParserException, UnfullfilledRequirementsException;


    int nextNonWhitespace() throws XmlPullParserException, IOException {
        int ret = parser.next();
        if (ret == KXmlParser.TEXT && parser.isWhitespace()) {
            ret = parser.next();
        }
        return ret;
    }


    // Copied from most recent KxmlParser in
    // https://github.com/stefanhaustein/kxml2/blob/87f02cdcd8e826e7741cfd5ae854c4b985c62218/src/main/java/org/kxml2/io/KXmlParser.java#L1434
    // Not currently in an android phone's embedded kxml library

    /**
     * Skip sub tree that is currently porser positioned on.
     * <br>NOTE: parser must be on START_TAG and when funtion returns
     * parser will be positioned on corresponding END_TAG.
     */

    //	Implementation copied from Alek's mail...

    public void skipSubTree() throws XmlPullParserException, IOException {
        parser.require(KXmlParser.START_TAG, null, null);
        int level = 1;
        while (level > 0) {
            int eventType = parser.next();
            if (eventType == KXmlParser.END_TAG) {
                --level;
            }
            else if (eventType == KXmlParser.START_TAG) {
                ++level;
            }
        }
    }

}
