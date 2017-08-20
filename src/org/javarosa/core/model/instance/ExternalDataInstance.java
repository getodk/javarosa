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

// This is still a work in progress.

public class ExternalDataInstance extends DataInstance {
    private TreeElement root;

    // todo Make @mdudzinski’s recommended changes from https://github.com/opendatakit/javarosa/pull/154#pullrequestreview-51806826

    public ExternalDataInstance(String path, String instanceId)
        /* todo implement error handling */ throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        super(instanceId);
        setName(instanceId);
        String absolutePath = /* ToDo: find out how to get the actual location */
                System.getProperty("user.dir") + "/resources" + path;
        KXmlParser xmlParser = ElementParser.instantiateParser(new FileInputStream(absolutePath));
        TreeElementParser treeElementParser = new TreeElementParser(xmlParser, 0, instanceId);
        root = treeElementParser.parse();
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

    /**
     * Returns the path of the URI at srcLocation if the scheme is <code>jr</code> and the host is
     * <code>file</code>, otherwise returns <code>null</code>.
     * @param srcLocation the value of the <code>src</code> attribute of the <code>instance</code> element
     * @throws URISyntaxException if srcLocation can’t be parsed as a URI
     */
    public static String getPathIfExternalDataInstance(String srcLocation) {
        if (srcLocation != null && !srcLocation.isEmpty()) {
            try {
                URI uri = new URI(srcLocation);
                if ("jr".equals(uri.getScheme()) && "file".equals(uri.getHost())) {
                    return uri.getPath();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace(); // ToDo: decide what errors to report and how to report them
            }
        }
        return null;
    }
}
