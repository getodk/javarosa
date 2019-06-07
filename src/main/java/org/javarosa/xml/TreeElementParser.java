package org.javarosa.xml;

import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
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
    public TreeElement parse() throws InvalidStructureException, IOException,
        XmlPullParserException, UnfullfilledRequirementsException {

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
     * Returns the internal secondary instance with the instance ID created
     * Used for deserializing
     * @return The TreeElement node of the internal instance
     * @throws IOException
     * @throws InvalidReferenceException
     * @throws InvalidStructureException
     * @throws XmlPullParserException
     * @throws UnfullfilledRequirementsException
     */
    public TreeElement parseInternalSecondaryInstance() throws IOException, InvalidReferenceException, InvalidStructureException, XmlPullParserException, UnfullfilledRequirementsException {
        final int depth = parser.getDepth();
        //instance node is assumed not to be the first node
        int foundInstanceIndex = -1;
        while (parser.getDepth() >= depth) {
            while (findInstanceNode()){
                //instance names are in the default namespace
                String instanceId = parser.getAttributeValue( null, ID_ATTR);
                if (this.instanceId.equals(instanceId)) {
                    return new TreeElementParser(parser, foundInstanceIndex, instanceId).parse();
                } else {
                    skipSubTree();
                }
            }
        }
        throw new InvalidReferenceException(String.format("The instance ID %s was not found in the XForm file ", instanceId),instanceId);
    }

    /**
     *
     * Parses the internal instances from an XForm (excluding the main instance)
     * @return List of #TreeElement representation of the internal
     * instance nodes.
     *
     * @throws IOException There was an error parsing the xml file
     * @throws InvalidStructureException
     * @throws XmlPullParserException
     * @throws UnfullfilledRequirementsException
     */
    public List<TreeElement> parseInternalSecondaryInstances() throws InvalidStructureException, XmlPullParserException, UnfullfilledRequirementsException, IOException {
        /**
         * This could be implemented using the XPath reference to target specifically
         * the path of the instance node, but currently, this targets any path where
         * <instance></instance> node is found
         */
        List<TreeElement> internalInstances = new ArrayList<>();
        final int depth = parser.getDepth();
        if (depth > 0) {
            while (parser.getDepth() >= depth) {
                while(findInstanceNode()){
                    TreeElement treeElement = new TreeElementParser(parser, 0, "").parse();
                    if(treeElement.getAttributeValue(null, ID_ATTR) !=null){
                        String instanceId = treeElement.getAttributeValue(null,ID_ATTR);
                        if(instanceId != null){
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

    public boolean findInstanceNode() throws XmlPullParserException, IOException {
        int ret = nextNonWhitespace();
        if (ret == Node.ELEMENT && parser.getName().equals(INSTANCE_ELEMENT)) {
            return true;
        } else if (ret != KXmlParser.END_TAG) {
            return false;
        } else {
            return findInstanceNode();
        }
    }

}
