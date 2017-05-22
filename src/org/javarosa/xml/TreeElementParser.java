package org.javarosa.xml;

import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Hashtable;

/**
 * @author ctsims
 */
public class TreeElementParser extends ElementParser<TreeElement> {
    final int multiplicity;
    final String instanceId;

    public TreeElementParser(KXmlParser parser, int multiplicity, String instanceId) {
        super(parser);
        this.multiplicity = multiplicity;
        this.instanceId = instanceId;
    }

    @Override
    public TreeElement parse() throws InvalidStructureException, IOException,
            XmlPullParserException, UnfullfilledRequirementsException {

        int depth = parser.getDepth();
        TreeElement element = new TreeElement(parser.getName(), multiplicity);
        element.setInstanceName(instanceId);
        for (int i = 0; i < parser.getAttributeCount(); ++i) {
            element.setAttribute(parser.getAttributeNamespace(i), parser.getAttributeName(i), parser.getAttributeValue(i));
        }

        Hashtable<String, Integer> multiplicities = new Hashtable<>();

        // loop parses all siblings at a given depth
        while (parser.getDepth() >= depth) {
            switch (this.nextNonWhitespace()) {
                case KXmlParser.START_TAG:
                    String name = parser.getName();
                    int val;
                    if (multiplicities.containsKey(name)) {
                        val = multiplicities.get(name) + 1;
                    } else {
                        val = 0;
                    }
                    multiplicities.put(name, new Integer(val));

                    TreeElement kid = new TreeElementParser(parser, val, instanceId).parse();
                    element.addChild(kid);
                    break;
                case KXmlParser.END_TAG:
                    return element;
                case KXmlParser.TEXT:
                    element.setValue(new UncastData(parser.getText().trim()));
                    break;
                default:
                    throw new InvalidStructureException("Exception while trying to parse an XML Tree, got something other than tags and text", parser);
            }
        }

        return element;
    }

}
