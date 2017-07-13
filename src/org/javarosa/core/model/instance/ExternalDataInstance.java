package org.javarosa.core.model.instance;

import org.javarosa.xml.ElementParser;
import org.javarosa.xml.TreeElementParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ExternalDataInstance extends DataInstance {
    private TreeElement root;

    public ExternalDataInstance(String srcLocation, String instanceId) {
        super(instanceId);
        setName(instanceId);
        try {
            URI uri = new URI(srcLocation);
            if ("jr".equals(uri.getScheme()) && "file".equals(uri.getHost())) {
                String absolutePath = /* ToDo: find out how to get the actual location */
                        System.getProperty("user.dir") + "/resources" + uri.getPath();
                KXmlParser xmlParser = ElementParser.instantiateParser(new FileInputStream(absolutePath));
                TreeElementParser treeElementParser = new TreeElementParser(xmlParser, 0, instanceId);
                root = treeElementParser.parse();
            }
        } catch (XmlPullParserException | IOException | URISyntaxException | UnfullfilledRequirementsException |
                InvalidStructureException e) {
            e.printStackTrace(); // ToDo: decide what errors to report and how to report them
        }
    }

    @Override
    public AbstractTreeElement getBase() {
        return root; // ToDo what should this be?
    }

    @Override
    public AbstractTreeElement getRoot() {
        return root;
    }

    @Override
    public void initialize(InstanceInitializationFactory initializer, String instanceId) {
        throw new RuntimeException("Not implemented");
    }
}
