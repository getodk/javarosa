package org.javarosa.xml;

import org.javarosa.core.model.instance.InternalDataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InternalDataInstanceParser {


    /**
     * Builds all of the <b>InternalDataInstances</b> contained of an
     * XForm
     *
     * Implemented as an alternative to parsing the each internal
     * instance
     *
     * @param xFormSrc the path  of the Xform containing internal instances…
     * @return List of all the #InternalDataInstance in the XForm
     * @throws IOException                       if FileInputStream can’t find the file, or ElementParser can’t read the stream
     * @throws InvalidReferenceException         if the ReferenceManager in getPath(String srcLocation) can’t derive a reference
     * @throws UnfullfilledRequirementsException thrown by {@link TreeElementParser#parse()}
     * @throws XmlPullParserException            thrown by {@link TreeElementParser#parse()}
     * @throws InvalidStructureException         thrown by {@link TreeElementParser#parse()}
     */
    public static List<InternalDataInstance> buildInstances(String xFormSrc)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {

        InputStream inputStream = new FileInputStream(xFormSrc);
        KXmlParser parser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser =
            new TreeElementParser(parser,0, "");

        List<TreeElement> instances = treeElementParser.parseInternalSecondaryInstances();
        List<InternalDataInstance> internalDataInstances = new ArrayList<>();
        for(TreeElement element: instances){
            String id = element.getAttribute(null, "id").getAttributeValue();
            InternalDataInstance internalDataInstance  = new InternalDataInstance(element.getChildAt(0), id,  xFormSrc);
            internalDataInstances.add(internalDataInstance);
        }
        return internalDataInstances;
    }


    /**
     * Builds the InternalDataInstances out of an XForm
     *
     * @param xFormSrc the path  of the Xform containing internal instances…
     * @return a list  new InternalDataInstance
     * @throws IOException                       if FileInputStream can’t find the file, or ElementParser can’t read the stream
     * @throws InvalidReferenceException         if the ReferenceManager in getPath(String srcLocation) can’t derive a reference
     * @throws UnfullfilledRequirementsException thrown by {@link TreeElementParser#parse()}
     * @throws XmlPullParserException            thrown by {@link TreeElementParser#parse()}
     * @throws InvalidStructureException         thrown by {@link TreeElementParser#parse()}
     */
    public static InternalDataInstance build( String xFormSrc, String instanceId)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {

        InputStream inputStream = new FileInputStream(xFormSrc);
        KXmlParser parser = ElementParser.instantiateParser(inputStream);
        TreeElementParser treeElementParser =
            new TreeElementParser(parser,0, instanceId);
        TreeElement treeElement = treeElementParser.parseInternalSecondaryInstance();
        if (treeElement.getNumChildren() == 0)
            throw new RuntimeException("Root TreeElement node has no children");
        InternalDataInstance internalDataInstance  = new InternalDataInstance(treeElement.getChildAt(0), instanceId,  xFormSrc);

        return internalDataInstance;
    }

    public static TreeElement parseInternalInstance(String xFormSrc, String instanceId) throws IOException, InvalidReferenceException, InvalidStructureException, XmlPullParserException, UnfullfilledRequirementsException {
        InputStream inputStream = new FileInputStream(xFormSrc);
        KXmlParser parser = ElementParser.instantiateParser(inputStream);
        TreeElement treeElement =  new TreeElementParser(parser, 0, instanceId).parseInternalSecondaryInstance();
        //Returns the instance child TreeElement, not the instance TreeElement
        if (treeElement.getNumChildren() == 0)
            throw new RuntimeException("Root TreeElement node has no children");
        return treeElement.getChildAt(0);
    }

}