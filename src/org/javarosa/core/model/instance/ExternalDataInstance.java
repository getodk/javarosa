package org.javarosa.core.model.instance;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xml.ElementParser;
import org.javarosa.xml.TreeElementParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// This is still a work in progress.

public class ExternalDataInstance extends DataInstance {
    private String path;
    private TreeElement root;

    // todo Make @mdudzinski’s recommended changes from https://github.com/opendatakit/javarosa/pull/154#pullrequestreview-51806826

    /** No-args constructor for deserialization */
    public ExternalDataInstance() {
    }

    public ExternalDataInstance(String path, String instanceId, TreeElement root) {
        super(instanceId);
        this.path = path;
        setName(instanceId);
        this.root = root;
    }

    public static ExternalDataInstance buildFromPath(String path, String instanceId)
            throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        String absolutePath = getPathPrefix() + path;
        KXmlParser xmlParser = ElementParser.instantiateParser(new FileInputStream(absolutePath));
        TreeElementParser treeElementParser = new TreeElementParser(xmlParser, 0, instanceId);
        TreeElement root = treeElementParser.parse();
        return new ExternalDataInstance(path, instanceId, root);
    }

    private static String getPathPrefix() {
        return System.getProperty("user.dir") + "/resources"; // ToDo find out how to get the actual location
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

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        super.readExternal(in, pf);
        path = ExtUtil.readString(in);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
   		super.writeExternal(out);
   		ExtUtil.write(out, path);
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
