package org.javarosa.xml;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author johnthebeloved
 *
 * <p>
 *     Alternative to using KXMLParser.parse() for creating
 *     KXML Documents.
 *     Element creation logic copied from from #TreeElementParser
 *     implementation, builds Element instead of #TreeElement  as
 *     done in #TreeElementParser.
 * </p>
 * <p>
 *      Also skips creating whitespace elements to improve parsing time.
 * </p>
 */
public class KXmlElementParser extends ElementParser<Element> {

    private NodeGatherer[] nodesToGather;
    private Element elementCreator;

    public KXmlElementParser(KXmlParser parser) {
        super(parser);
        elementCreator = new Element();
    }

    public KXmlElementParser(KXmlParser parser, NodeGatherer...nodesToGather) {
        this(parser);
        this.nodesToGather = nodesToGather;
    }

    /**
     * Creates a KXML Document from the KXML parser
     * and skips creating whitespace nodes
     *
     * @return The Document analogous to doc.parse() in KXML
     * @throws IOException There was an issue reading the XML File
     * @throws XmlPullParserException There was an issue Parsing the XML File
     */
    public Document parseDoc() throws IOException, XmlPullParserException {
        Document document = new Document();
        Element root = parse();
        document.addChild(Node.ELEMENT, root);
        return document;
    }

    /**
     * Parses the current parser into an ELEMENT
     * from the current position of the KXMLParser instance used
     *
     * @return KXML Element which is the Element at the current
     * parsing position
     * @throws IOException There was an issue reading the XML File
     * @throws XmlPullParserException There was an issue Parsing the XML File
     */
    public Element parse()
        throws IOException, XmlPullParserException {
        final int depth = parser.getDepth();
        Element element = initCurrentElement();
        final Map<String, Integer> multiplicitiesByName = new HashMap();
        while (parser.getDepth() >= depth) {
            switch (nextNonWhitespace()) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    NodeGatherer nodeGatherer = shouldGatherNode(name, nodesToGather);
                    if (nodeGatherer != null) {
                        Element elementToSkip = initCurrentElement();
                        element.addChild(Node.ELEMENT,elementToSkip);
                        String nodeString = writeToString();
                        nodeGatherer.add(nodeString);
                    } else {
                        final Integer multiplicity = multiplicitiesByName.get(name);
                        int newMultiplicity = (multiplicity != null) ? multiplicity + 1 : 0;
                        multiplicitiesByName.put(name, newMultiplicity);
                        Element childElement = parse();
                        element.addChild(Node.ELEMENT, childElement);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    return element;
                case XmlPullParser.TEXT:
                    if (parser.getText() != null)
                        element.addChild(Node.TEXT, parser.getText().trim());
                    break;
                default:
                    throw new XmlPullParserException(
                        "Exception while trying to parse an XML Tree, got something other than tags and text");
            }
        }
        return  element;

    }

    /**
     * Check to see if parser should
     * gather the child node instead of parsing
     * of the provided element name
     *
     * @param elementName The name of the current Element being parsed.
     * @param nodesToGather Representation of predefined nodes
     *                       intended to be gathered
     * @return if the provided node should be gathered
     */
    private NodeGatherer shouldGatherNode(String elementName, NodeGatherer ...nodesToGather){
        if(nodesToGather != null){
            for(int e = 0; e < nodesToGather.length; e++){
                NodeGatherer NodeGatherer = nodesToGather[e];
                if(NodeGatherer.skip(elementName)){
                    return NodeGatherer;
                }
            }
        }
        return null;
    }

    /**
     * Sets the namespaces and attribute nodes of the current element
     * @return Element
     */
    private Element initCurrentElement(){
        Element element = elementCreator.createElement(parser.getNamespace(), parser.getName());
        for (int i = parser.getNamespaceCount (parser.getDepth () - 1);
             i < parser.getNamespaceCount (parser.getDepth ()); i++) {
            element.setPrefix (parser.getNamespacePrefix (i), parser.getNamespaceUri(i));
        }

        for (int i = 0; i < parser.getAttributeCount(); ++i) {
            element.setAttribute(parser.getAttributeNamespace(i), parser.getAttributeName(i), parser.getAttributeValue(i));
        }
        return element;
    }

}
