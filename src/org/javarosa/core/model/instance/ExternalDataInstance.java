package org.javarosa.core.model.instance;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xml.ElementParser;
import org.javarosa.xml.TreeElementParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

// This is still a work in progress.

public class ExternalDataInstance extends DataInstance {
    private static final Logger logger = LoggerFactory.getLogger(ExternalDataInstance.class.getSimpleName());
    private String path;
    private TreeElement root;

    // todo Make @mdudzinski’s recommended changes from https://github.com/opendatakit/javarosa/pull/154#pullrequestreview-51806826

    /** No-args constructor for deserialization */
    public ExternalDataInstance() {
    }

    private ExternalDataInstance(TreeElement root, String instanceId, String path) {
        super(instanceId);
        this.path = path;
        setName(instanceId);
        setRoot(root);
    }

    /**
     * Builds an ExternalDataInstance
     *
     * @param instanceSrc       the value of the instance’s src attribute, e.g., jr://file/…
     * @param instanceId the ID of the new instance
     * @return a new ExternalDataInstance
     * @throws IOException                       if FileInputStream can’t find the file, or ElementParser can’t read the stream
     * @throws InvalidReferenceException         if the ReferenceManager in getPath(String srcLocation) can’t derive a reference
     * @throws UnfullfilledRequirementsException thrown by {@link TreeElementParser#parse()}
     * @throws XmlPullParserException            thrown by {@link TreeElementParser#parse()}
     * @throws InvalidStructureException         thrown by {@link TreeElementParser#parse()}
     */
    public static ExternalDataInstance build(String instanceSrc, String instanceId)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException, InvalidReferenceException {
        TreeElement root = parseExternalInstance(instanceSrc, instanceId);
        return new ExternalDataInstance(root, instanceId, instanceSrc);
    }

    private static TreeElement parseExternalInstance(String instanceSrc, String instanceId) throws IOException, InvalidReferenceException, InvalidStructureException, XmlPullParserException, UnfullfilledRequirementsException {
        KXmlParser xmlParser = ElementParser.instantiateParser(new FileInputStream(getPath(instanceSrc)));
        TreeElementParser treeElementParser = new TreeElementParser(xmlParser, 0, instanceId);
        return treeElementParser.parse();
    }

    @Override
    public AbstractTreeElement getBase() {
        return root;
    }

    @Override
    public AbstractTreeElement getRoot() {
        if (root.getNumChildren() == 0)
            throw new RuntimeException("root node has no children");

        return root.getChildAt(0);
    }

    private void setRoot(TreeElement topLevel) {
        root = new TreeElement();
        root.setInstanceName(getName());
        root.addChild(topLevel);
    }

    @Override
    public void initialize(InstanceInitializationFactory initializer, String instanceId) {
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        super.readExternal(in, pf);
        path = ExtUtil.readString(in);
        try {
            setRoot(parseExternalInstance(path, getInstanceId()));
        } catch (InvalidReferenceException | InvalidStructureException | XmlPullParserException | UnfullfilledRequirementsException e) {
            throw new DeserializationException("Unable to parse external instance: " + e);
        }
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
        ExtUtil.write(out, path);
    }

    /**
     * Returns the path of the URI at srcLocation.
     * @param srcLocation the value of the <code>src</code> attribute of the <code>instance</code> element
     */
    private static String getPath(String srcLocation) throws InvalidReferenceException {
        String uri = ReferenceManager.instance().DeriveReference(srcLocation).getLocalURI();
        return uri.startsWith("//") /* todo why is this? */ ? uri.substring(1) : uri;
    }
}
