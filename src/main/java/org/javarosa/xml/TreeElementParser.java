package org.javarosa.xml;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xpath.eval.Indexer;
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

        if(currentTreeReference == null){
            currentTreeReference = TreeReference.rootRef();
            currentTreeReference.setInstanceName(element.getInstanceName());
            currentTreeReference.setContext(TreeReference.CONTEXT_INSTANCE);
            currentTreeReference.add(element.getName(), 0);
        }

        final Map<String, Integer> multiplicitiesByName = new HashMap<>();

        // loop parses all siblings at a given depth
        while (parser.getDepth() >= depth) {
            switch (nextNonWhitespace()) {
                case KXmlParser.START_TAG:
                    String name = parser.getName();
                    final Integer multiplicity = multiplicitiesByName.get(name);
                    int newMultiplicity = (multiplicity != null) ? multiplicity + 1 : 0;

                    currentTreeReference.add(name, newMultiplicity);

                    multiplicitiesByName.put(name, newMultiplicity);
                    TreeElement childTreeElement = new TreeElementParser(parser, newMultiplicity, instanceId).parse();
                    element.addChild(childTreeElement);
                    break;
                case KXmlParser.END_TAG:
                    currentTreeReference.removeLastLevel();
                    if(currentTreeReference.size() == 0){
                        for(Indexer indexer: indexers) {
                            indexer.clearCaches();
                            currentTreeReference = null;
                        }
                    }
                    return element;
                case KXmlParser.TEXT:
                    element.setValue(new UncastData(parser.getText().trim()));
                    for(Indexer indexer: indexers) {
                        if(indexer.belong(currentTreeReference)){
                            indexer.addToIndex(currentTreeReference, element);
                        }
                    }
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
            boolean primaryInstanceSkipped = false;
            while (parser.getDepth() >= depth) {
                nextNonWhitespace();

                if (currentNodeIsInternalInstance()) {
                    // The primary instance is the first instance defined
                    if (!primaryInstanceSkipped) {
                        primaryInstanceSkipped = true;
                    } else {
                        TreeElement treeElement = new TreeElementParser(parser, 0, "").parse();
                        String instanceId = treeElement.getAttributeValue(null, ID_ATTR);
                        if (instanceId != null) {
                            treeElement.setInstanceName(instanceId);
                        }

                        internalInstances.add(treeElement);
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

    // Added for indexing
    private static TreeReference currentTreeReference = null;
    public static List<Indexer> indexers = new ArrayList<>();
    public static List<IConditionExpr> indexedExpressions = new ArrayList<>();

    public static List<TreeReference> getNodeset(TreeReference treeReference){
        for (Indexer indexer : indexers) {
            if(indexer.belong(treeReference)){
                List<TreeReference> nodesetReferences = indexer.getFromIndex(treeReference);
                if (nodesetReferences != null) {
                    return nodesetReferences;
                }
            }
        }
        return null;
    }

    public static IAnswerData getRVFromIndex(TreeReference treeReference){

        for (Indexer indexer : indexers) {
            if(indexer.belong(treeReference)) {
                IAnswerData rawValue = indexer.getRawValueFromIndex(treeReference);
                if (rawValue != null) {
                    return rawValue;
                }
            }
        }
        return null;
    }

    //TODO:This may not be entirely correct
    public static boolean indexed(TreeReference treeReference){
        for(Indexer indexer : indexers ){
            if(indexer.belong(treeReference)){
                if(indexer.predicateSteps.length > 0 &&
                    treeReference.getPredicate(indexer.predicateSteps[0].stepIndex) != null)
                    return true;
            }
        }
        return false;
    }
}
