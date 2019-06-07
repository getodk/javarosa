package org.javarosa.core.model.instance;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xml.InternalDataInstanceParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Representation of an internal secondary data instance.
 *
 * Created from implementation of #ExternalDataInstance with few differences.
 *
 * The path to XForm is stored instead of the path to the external secondary data instance file
 *
 * @author johnthebeloved
 *
 */
public class InternalDataInstance extends DataInstance {
    private String xFormPath;
    private TreeElement root;

    /**
     * No-args constructor for deserialization
     */
    public InternalDataInstance() {
    }

    /**
     *
     * @param root The TreeElement represents the first child element of the internal
     *             instance since internal instance always have only one child
     *             which is the root of the ItemSets
     * @param instanceId
     * @param xFormPath
     */
    public InternalDataInstance(TreeElement root, String instanceId, String xFormPath) {
        super(instanceId);
        this.xFormPath = xFormPath;
        setName(instanceId);
        setRoot(root);
    }


    /**
     *  Parses the XForm file to a TreeElement instead of Element in
     *  * KXML Library.
     * @return
     */

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
        xFormPath = ExtUtil.readString(in);
        try {
            setRoot(InternalDataInstanceParser.parseInternalInstance(xFormPath, getInstanceId()));
        } catch (InvalidReferenceException | InvalidStructureException | XmlPullParserException | UnfullfilledRequirementsException e) {
            throw new DeserializationException("Unable to parse external instance: " + e);
        }
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
        ExtUtil.write(out, xFormPath);
    }

}