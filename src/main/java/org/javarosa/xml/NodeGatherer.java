package org.javarosa.xml;


import java.util.ArrayList;
import java.util.List;

/**
 * @author johnthebeloved
 * Abstracts algorithm for skipping child elements
 * This could be implemented with XPath reference
 * Also assumes that the elements to be skipped are siblings
 */
public class NodeGatherer
{
    /**
     * Signifies skipping should be from the start sibling to the last sibling
     */
    public static final int LAST_ELEMENT_INDEX = -1;
    private String elementName;
    private int from;
    private int to;
    private int currentParsingIndex;
    private List<String> xmlTree;

    /**
     * Starts skipping  from the provided from parameter to the last sibling
     * @param elementName  The name of the element to skip
     * @param from the currentParsingIndex to begin skipping subtrees
     *
     */
    public NodeGatherer(String elementName, int from){
        this(elementName,from, LAST_ELEMENT_INDEX);
    }

    /**
     * Starts skipping  from the provided from parameter to the last sibling
     * @param elementName  The name of the element to skip
     * @param from the multiplicity currentParsingIndex to begin skipping
     * @param to the multiplicity currentParsingIndex to end skipping
     */
    public NodeGatherer(String elementName, int from, int to){
        this.from = from;
        this.to = to;
        currentParsingIndex = 0;
        this.elementName = elementName;
        this.xmlTree = new ArrayList<>();
    }

    public boolean skip(String elementName){
        if(this.elementName.equals(elementName)){
            boolean skip = (currentParsingIndex >= from && (currentParsingIndex <= to || to == LAST_ELEMENT_INDEX));
            currentParsingIndex +=1;
            return skip;
        }
        return false;
    }

    public void add(String xmlTree){
        this.xmlTree.add(xmlTree);
    }

    public List<String> getXmlTrees(){
        return xmlTree;
    }

}