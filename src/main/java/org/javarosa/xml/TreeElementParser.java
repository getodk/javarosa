package org.javarosa.xml;

import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xml.util.InvalidStructureException;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ctsims
 * @author johnthebeloved
 */
public class TreeElementParser extends ElementParser<TreeElement> {
    private final int multiplicity;
    private final String instanceId;

    private static final String INSTANCE_ELEMENT = "instance";
    private static final String ID_ATTR = "id";

    public TreeElementParser(KXmlParser parser, int multiplicity, String instanceId) {
        super(parser);
        this.multiplicity = multiplicity;
        this.instanceId = instanceId;
    }

    @Override
    public TreeElement parse() throws InvalidStructureException, IOException, XmlPullParserException {
        final int depth = parser.getDepth();
        final TreeElement element = new TreeElement(parser.getName(), multiplicity);
        element.setInstanceName(instanceId);
        for (int i = 0; i < parser.getAttributeCount(); ++i) {
            element.setAttribute(parser.getAttributeNamespace(i), parser.getAttributeName(i), parser.getAttributeValue(i));
        }

        final Map<String, Integer> multiplicitiesByName = new HashMap<>();

        // loop parses all siblings at a given depth
        while (parser.getDepth() >= depth) {
            switch (nextNonWhitespace()) {
                case KXmlParser.START_TAG:
                    String name = parser.getName();
                    final Integer multiplicity = multiplicitiesByName.get(name);
                    int newMultiplicity = (multiplicity != null) ? multiplicity + 1 : 0;
                    multiplicitiesByName.put(name, newMultiplicity);
                    TreeElement childTreeElement = new TreeElementParser(parser, newMultiplicity, instanceId).parse();
                    element.addChild(childTreeElement);
                    break;
                case KXmlParser.END_TAG:
                    return element;
                case KXmlParser.TEXT:
                    element.setValue(new UncastData(parser.getText().trim()));
                    break;
                default:
                    throw new InvalidStructureException(
                        "Exception while trying to parse an XML Tree, got something other than tags and text", parser);
            }
        }

        return element;
    }

    /**
     * Parses the internal instances from an XForm (excluding the main instance)
     * @return List of #TreeElement representation of the internal
     * instance nodes.
     *
     * @throws IOException There was an error parsing the xml file
     * @throws InvalidStructureException
     * @throws XmlPullParserException
     */
    public List<TreeElement> parseInternalSecondaryInstances() throws InvalidStructureException, XmlPullParserException, IOException {
        List<TreeElement> internalInstances = new ArrayList<>();
        final int depth = parser.getDepth();
        if (depth > 0) {
            while (parser.getDepth() >= depth) {
                nextNonWhitespace();

                if (currentNodeIsInternalInstance()) {
                    TreeElement treeElement = new TreeElementParser(parser, 0, "").parse();

                    if (treeElement.getAttributeValue(null, ID_ATTR) != null) {
                        String instanceId = treeElement.getAttributeValue(null,  ID_ATTR);
                        if (instanceId != null) {
                            treeElement.setInstanceName(instanceId);
                            internalInstances.add(treeElement);
                        }
                    }
                }
            }
            return internalInstances;
        } else {
            throw new InvalidStructureException("Invalid XML File, no detected xml node - Depth is 0");
        }
    }

    private boolean currentNodeIsInternalInstance() throws XmlPullParserException {
        return parser.getEventType() == Node.ELEMENT
            && parser.getName().equals(INSTANCE_ELEMENT)
            && parser.getAttributeValue(null,"src") == null;
    }
}
