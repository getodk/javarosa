package org.javarosa.xml;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xpath.eval.Indexer;
import org.javarosa.xpath.eval.IndexerResolver;
import org.javarosa.xpath.eval.IndexerType;
import org.javarosa.xpath.eval.PredicateStep;
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
       return parse(null, null);
    }

    @Override
    public TreeElement parse(TreeReference currentTreeReference, IndexerResolver indexerResolver) throws InvalidStructureException, IOException, XmlPullParserException {
        final int depth = parser.getDepth();
        final TreeElement element = new TreeElement(parser.getName(), multiplicity);
        element.setInstanceName(instanceId);
        for (int i = 0; i < parser.getAttributeCount(); ++i) {
            element.setAttribute(parser.getAttributeNamespace(i), parser.getAttributeName(i), parser.getAttributeValue(i));
        }

        //#Indexation: Create a root TreeReference
        if(indexerResolver != null && indexerResolver.getIndexers().size() > 0)
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

                    //#Indexation: update current TreeReference
                    if(indexerResolver != null && indexerResolver.getIndexers().size() > 0)
                    currentTreeReference.add(name, newMultiplicity);

                    multiplicitiesByName.put(name, newMultiplicity);
                    TreeElement childTreeElement = new TreeElementParser(parser, newMultiplicity, instanceId).parse(currentTreeReference,  indexerResolver);
                    element.addChild(childTreeElement);
                    break;
                case KXmlParser.END_TAG:
                    //#Indexation
                    if(indexerResolver != null && indexerResolver.getIndexers().size() > 0) {
                        //update current TreeReference
                        currentTreeReference.removeLastLevel();
                        //if the tree reference is at the end of the document
                        //Call the finalize to persist the index if necessary
                        if (currentTreeReference.size() == 0) {
                            for (Indexer indexer : indexerResolver.getIndexers()) {
                                indexer.finalizeIndex();
                                currentTreeReference = null;
                            }
                        } else if (parser.getDepth() == depth) { //Index this element if it should be
                            for (Indexer indexer : indexerResolver.getIndexers()) {
                                if (indexer.belong(currentTreeReference) && indexer.getIndexerType().equals(IndexerType.GENERIC_PATH)) {
                                    indexer.addToIndex(currentTreeReference, element);
                                }
                            }
                        }
                    }
                    return element;
                case KXmlParser.TEXT:
                    element.setValue(new UncastData(parser.getText().trim()));

                    //#Indexation
                    //Only generic paths are refIsIndexed like this
                    if(indexerResolver != null && indexerResolver.getIndexers().size() > 0)
                    for(Indexer indexer: indexerResolver.getIndexers()) {
                        if(indexer.belong(currentTreeReference) && !indexer.getIndexerType().equals(IndexerType.GENERIC_PATH)){
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

}
