package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.TreeElementParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

// This is still a work in progress.

public class ExternalDataInstance extends DataInstance {
    public static final TreeElement PLACEHOLDER_ROOT = new TreeElement("missing file", 0);
    private static final Logger logger = LoggerFactory.getLogger(XFormParser.class.getSimpleName());

    private String path;
    private TreeElement root;

    // todo Make @mdudzinski’s recommended changes from https://github.com/getodk/javarosa/pull/154#pullrequestreview-51806826

    /**
     * No-args constructor for deserialization
     */
    public ExternalDataInstance() {
    }

    private ExternalDataInstance(TreeElement root, String instanceId, String path) {
        super(instanceId);
        this.path = path;
        setName(instanceId);
        setRoot(root);
    }

    /**
     * Builds an ExternalDataInstance by parsing the file at the given location or fills in a placeholder if the file
     * can't be found or derived.
     *
     * The placeholder makes it possible to successfully parse a form without an external
     * instance in cases where a client isn't providing a form-filling interface (e.g. form validation or discovery).
     *
     * @param instanceSrc the value of the instance’s src attribute, e.g., jr://file/…
     * @param instanceId  the ID of the new instance
     * @return a new ExternalDataInstance
     * @throws IOException                       if ElementParser can’t read the stream
     * @throws UnfullfilledRequirementsException thrown by {@link TreeElementParser#parse()}
     * @throws XmlPullParserException            thrown by {@link TreeElementParser#parse()}
     * @throws InvalidStructureException         thrown by {@link TreeElementParser#parse()}
     */
    public static ExternalDataInstance build(String instanceSrc, String instanceId)
        throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement root;
        try {
            root = parseExternalInstance(instanceSrc, instanceId);
        } catch (FileNotFoundException | InvalidReferenceException e) {
            logger.info("External instance not found, falling back to placeholder");
            root = PLACEHOLDER_ROOT;
        }
        return new ExternalDataInstance(root, instanceId, instanceSrc);
    }

    private static TreeElement parseExternalInstance(String instanceSrc, String instanceId) throws IOException, InvalidReferenceException, InvalidStructureException, XmlPullParserException, UnfullfilledRequirementsException {
        String path = getPath(instanceSrc);
        return instanceSrc.contains("file-csv") ?
            CsvExternalInstance.parse(instanceId, path) : XmlExternalInstance.parse(instanceId, path);
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

    public boolean isUsingPlaceholder() {
        return getRoot().equals(PLACEHOLDER_ROOT);
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
     *
     * @param srcLocation the value of the <code>src</code> attribute of the <code>instance</code> element
     */
    private static String getPath(String srcLocation) throws InvalidReferenceException {
        String uri = ReferenceManager.instance().deriveReference(srcLocation).getLocalURI();
        return uri.startsWith("//") /* todo why is this? */ ? uri.substring(1) : uri;
    }
}
