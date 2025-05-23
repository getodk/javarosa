package org.javarosa.core.model.instance;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A data instance represents a tree structure of abstract tree
 * elements which can be accessed and read with tree references. It is
 * a supertype of different types of concrete models which may or may not
 * be read only.
 *
 * @author ctsims
 *
 */
public abstract class DataInstance<T extends AbstractTreeElement<T>> implements Persistable {
    /** The integer Id of the model */
    private int recordid = -1;

    /**
     * A unique name for this instance. The value of the id attribute in the case of a secondary instance or the title
     * of the form for the primary instance.
     **/
    private String name;

    /** The ID of the form that this is a model for */
    private int formId;

    private String instanceid;

    public DataInstance() {

    }


    public DataInstance(String instanceid) {
        this.instanceid = instanceid;
    }

    public static TreeReference unpackReference(IDataReference ref) {
        return (TreeReference) ref.getReference();
    }

    public abstract AbstractTreeElement<T> getBase();

    public abstract T getRoot();

    public String getInstanceId() {
        return instanceid;
    }

    protected void setInstanceId(String instanceid) {
        this.instanceid = instanceid;
    }
    /**
     * Whether the structure of this instance is only available at runtime.
     *
     * @return true if the instance structure is available and runtime and can't
     * be checked for consistency until the reference is made available. False
     * otherwise.
     *
     */
    public boolean isRuntimeEvaluated() {
        return false;
    }

    public T resolveReference(TreeReference ref) {
        if (!ref.isAbsolute()){
            return null;
        }

        AbstractTreeElement<T> node = getBase();
        T result = null;
        for (int i = 0; i < ref.size(); i++) {
            String name = ref.getName(i);
            int mult = ref.getMultiplicity(i);

            if(mult == TreeReference.INDEX_ATTRIBUTE) {
                //Should we possibly just return here?
                //I guess technically we could step back...
                node = result = node.getAttribute(null, name);
                continue;
            }
            if (mult == TreeReference.INDEX_UNBOUND) {
                if (node.getChildMultiplicity(name) == 1) {
                    mult = 0;
                } else {
                    // reference is not unambiguous
                    node = result = null;
                    break;
                }
            }

            node = result = node.getChild(name, mult);
            if (node == null) {
                break;
            }
        }

        if (node == getBase() || result == null) {
            return null; // never return a reference to '/'
        } else if (((TreeElement) result).isPartial()) {
            throw new PartialElementEncounteredException();
        } else {
            return result;
        }
    }

    public List<AbstractTreeElement<T>> explodeReference(TreeReference ref) {
        if (!ref.isAbsolute())
            return null;

      List<AbstractTreeElement<T>> nodes = new ArrayList<AbstractTreeElement<T>>(ref.size());
        AbstractTreeElement<T> cur = getBase();
        for (int i = 0; i < ref.size(); i++) {
            String name = ref.getName(i);
            int mult = ref.getMultiplicity(i);

            //If the next node down the line is an attribute
            if(mult == TreeReference.INDEX_ATTRIBUTE) {
                //This is not the attribute we're testing
                if(cur != getBase()) {
                    //Add the current node
                    nodes.add(cur);
                }
                cur = cur.getAttribute(null, name);
            }

            //Otherwise, it's another child element
            else {
                if (mult == TreeReference.INDEX_UNBOUND) {
                    if (cur.getChildMultiplicity(name) == 1) {
                        mult = 0;
                    } else {
                        // reference is not unambiguous
                        return null;
                    }
                }

                if (cur != getBase()) {
                    nodes.add(cur);
                }

                cur = cur.getChild(name, mult);
                if (cur == null) {
                    return null;
                }
            }
        }
        return nodes;
    }

    public T getTemplate(TreeReference ref) {
        T node = getTemplatePath(ref);
        return (node == null ? null : ((node.isRepeatable() || node.isAttribute()) ? node : null));
    }

    public T getTemplatePath(TreeReference ref) {
        if (!ref.isAbsolute())
            return null;

        T walker = null;
        AbstractTreeElement<T> node = getBase();
        for (int i = 0; i < ref.size(); i++) {
            String name = ref.getName(i);

            if(ref.getMultiplicity(i) == TreeReference.INDEX_ATTRIBUTE) {
                node = walker = node.getAttribute(null, name);
            } else {

                T newNode = node.getChild(name, TreeReference.INDEX_TEMPLATE);
                if (newNode == null) {
                    newNode = node.getChild(name, 0);
                }
                if (newNode == null) {
                    return null;
                }
                node = walker = newNode;
            }
        }

        return walker;
    }

    /**
     * Determines if a path exists for a reference; template elements are
     * followed when available. Non-absolute references aren't followed.
     *
     * @param ref the reference path to be followed
     * @return was a valid path found for the reference?
     */
    public boolean hasTemplatePath(TreeReference ref) {
        return ref.isAbsolute() && hasTemplatePathRec(ref, getBase(), 0);
    }

    /**
     * Determines if a path exists for a reference using a given node; template
     * nodes followed first when available.
     *
     * @param topRef      the reference path being followed
     * @param currentNode the current element we are at along the path
     * @param depth       the depth of the current element
     * @return was a valid path found?
     */
    private boolean hasTemplatePathRec(TreeReference topRef, AbstractTreeElement<T> currentNode, int depth) {
        // stop when at the end of reference
        if (depth == topRef.size()) {
            return true;
        }
        // stop if we are trying to proceed on a null element
        if (currentNode == null) {
            return false;
        }

        String name = topRef.getName(depth);

        if (topRef.getMultiplicity(depth) == TreeReference.INDEX_ATTRIBUTE) {
            // recur on attribute node if the multiplicity designates it
            return hasTemplatePathRec(topRef, currentNode.getAttribute(null, name), depth + 1);
        } else {
            // try to grab template node
            T nextNode = currentNode.getChild(name, TreeReference.INDEX_TEMPLATE);
            if (nextNode != null) {
                return hasTemplatePathRec(topRef, nextNode, depth + 1);
            } else {
                // if there isn't a template element, recur through normal children
                // looking for the first valid path forward
                List<T> children = currentNode.getChildrenWithName(name);
                for (T child : children) {
                    if (hasTemplatePathRec(topRef, child, depth + 1)) {
                        // stop if we found a path
                        return true;
                    }
                }
            }
        }
        // no way forward
        return false;
    }

    public T resolveReference(IDataReference binding) {
        return resolveReference(unpackReference(binding));
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getFormId() {
        return this.formId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        String name = "NULL";
        if(this.name != null)
        {
            name = this.name;
        }
        return name;
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        recordid = ExtUtil.readInt(in);
        formId = ExtUtil.readInt(in);
        name = (String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf);
        instanceid = (String) ExtUtil.nullIfEmpty(ExtUtil.readString(in));

    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, recordid);
        ExtUtil.writeNumeric(out, formId);
        ExtUtil.write(out, new ExtWrapNullable(name));
        ExtUtil.write(out, ExtUtil.emptyIfNull(instanceid));
    }


    public int getID() {
        return recordid;
    }

    public void setID(int recordid) {
        this.recordid = recordid;
    }



    public abstract void initialize(InstanceInitializationFactory initializer, String instanceId);

    public void replacePartialElements(List<TreeElement> elements) {
        for (TreeElement element : elements) {
            TreeElement root = (TreeElement) getRoot();
            TreeElement matchingChild = root.getChild(element.getName(), element.getMultiplicity());

            if (matchingChild != null) {
                matchingChild.populatePartial(element);
            }
        }
    }

    public static class PartialElementEncounteredException extends RuntimeException {

    }
}