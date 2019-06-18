package org.javarosa.xml;

import org.javarosa.core.model.instance.InternalDataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class InternalDataInstanceParser {



    public static List<InternalDataInstance> buildInstances(List<String> dataInstanceXmlStrings)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {
        List<InternalDataInstance> internalDataInstances = new ArrayList<>();
        for(String dataInstanceXmlString: dataInstanceXmlStrings){
            internalDataInstances.add(build(dataInstanceXmlString));
        }
        return internalDataInstances;
    }


    /**
     * Builds the InternalDataInstance out of an xml String
     *
     * @param dataInstanceXmlString the string  which represents the internal data instance…
     * @return a list  new InternalDataInstance
     * @throws IOException                       if FileInputStream can’t find the file, or ElementParser can’t read the stream
     * @throws InvalidReferenceException         if the ReferenceManager in getPath(String srcLocation) can’t derive a reference
     * @throws UnfullfilledRequirementsException thrown by {@link TreeElementParser#parse()}
     * @throws XmlPullParserException            thrown by {@link TreeElementParser#parse()}
     * @throws InvalidStructureException         thrown by {@link TreeElementParser#parse()}
     */
    public static InternalDataInstance build(String dataInstanceXmlString)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {
        StringReader reader = new StringReader(dataInstanceXmlString);
        KXmlParser parser = ElementParser.instantiateParser(reader);
        TreeElementParser treeElementParser =
            new TreeElementParser(parser,0, "");
        TreeElement treeElement = treeElementParser.parse();
        if (treeElement.getNumChildren() == 0)
            throw new RuntimeException("Root TreeElement node has no children");
        String instanceId = treeElement.getAttributeValue("","id");
        InternalDataInstance internalDataInstance  = new InternalDataInstance(treeElement.getChildAt(0), instanceId,  dataInstanceXmlString);
        return internalDataInstance;
    }

    /**
     * Builds the InternalDataInstance out of an xml String
     *
     * @param dataInstanceXmlString the string  which represents the internal data instance…
     * @return a list  new InternalDataInstance
     * @throws IOException                       if FileInputStream can’t find the file, or ElementParser can’t read the stream
     * @throws InvalidReferenceException         if the ReferenceManager in getPath(String srcLocation) can’t derive a reference
     * @throws UnfullfilledRequirementsException thrown by {@link TreeElementParser#parse()}
     * @throws XmlPullParserException            thrown by {@link TreeElementParser#parse()}
     * @throws InvalidStructureException         thrown by {@link TreeElementParser#parse()}
     */
    public static TreeElement buildRoot(String dataInstanceXmlString)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {
        StringReader reader = new StringReader(dataInstanceXmlString);
        KXmlParser parser = ElementParser.instantiateParser(reader);
        TreeElementParser treeElementParser =
            new TreeElementParser(parser,0, "");
        return treeElementParser.parse();
    }


}