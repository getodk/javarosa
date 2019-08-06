package org.javarosa.xml;

import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xml.util.InvalidStructureException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author johnthebeloved
 * Parses the internal secondary instances out from an Xform file
 */
public class InternalDataInstanceParser {
    /**
     * Builds all of the internal secondary instances from a form definition.
     *
     * @param xFormSrc the path  of the Xform containing internal instances…
     * @return List of all the #InternalDataInstance in the XForm
     */
    public static HashMap<String, DataInstance> buildInstances(String xFormSrc) {
            HashMap<String, DataInstance> internalDataInstances = new HashMap<>();
            try {
                InputStream inputStream  = new FileInputStream(xFormSrc);
                KXmlParser parser = ElementParser.instantiateParser(inputStream);
                TreeElementParser treeElementParser =
                    new TreeElementParser(parser,0, "");

                List<TreeElement> internalInstancesTreeElementList = treeElementParser.parseInternalSecondaryInstances();

                for (TreeElement treeElement: internalInstancesTreeElementList) {
                    String id = treeElement.getAttribute(null, "id").getAttributeValue();
                    DataInstance internalDataInstance  = new FormInstance(treeElement.getChildAt(0), id);
                    internalDataInstances.put(internalDataInstance.getInstanceId(), internalDataInstance);
                }
            } catch (IOException | InvalidStructureException | XmlPullParserException e) {
                e.printStackTrace();
            }
        return internalDataInstances;
    }
}