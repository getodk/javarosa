/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.instance;

import org.javarosa.core.util.DataUtil;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.expr.XPathExpression;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TreeReference implements Externalizable, Serializable {
    /** @deprecated replaced by {@link #DEFAULT_MULTIPLICITY} */
    @Deprecated public static final int DEFAULT_MUTLIPLICITY = 0;

    /**
     * Multiplicity demarcates the position of a given element with respect to other elements
     * of the same name.
     *
     * Since users usually want to select the first instance from the nodeset
     * returned from a reference query, let the default multiplicity be selecting the first node.
     */
    public static final int DEFAULT_MULTIPLICITY = 0;

    /** Refers to all instances of an element, e.g. /data/b[-1] refers to b[0] and b[1] */
    public static final int INDEX_UNBOUND = -1;

    /**
     * 'repeats' (sections of a form that can multiply themselves) are populated with a
     * template that never exists in the form (IE: If you serialized the form to XML
     * it wouldn't be there) but provides the xml structure that should be replicated
     * when a 'repeat' is added
     */
    public static final int INDEX_TEMPLATE = -2;

    /** Multiplicity flag for an attribute */
    public static final int INDEX_ATTRIBUTE = -4;//multiplicity flag for an attribute

    public static final int INDEX_REPEAT_JUNCTURE = -10;

    //TODO: Roll these into RefLevel? Or more likely, take absolute
    //ref out of refLevel
    public static final int CONTEXT_ABSOLUTE = 0;
    public static final int CONTEXT_INHERITED = 1;
    public static final int CONTEXT_ORIGINAL = 2;
    public static final int CONTEXT_INSTANCE = 4;


    public static final int REF_ABSOLUTE = -1;

    public static final String NAME_WILDCARD = "*";

    /** -1 = absolute, 0 = context node, 1 = parent, 2 = grandparent ... */
    private int refLevel;
    private int contextType;
    private String instanceName = null;
    private List<TreeReferenceLevel> data = null;


    public static TreeReference rootRef () {
        TreeReference root = new TreeReference();
        root.refLevel = REF_ABSOLUTE;
        root.contextType = CONTEXT_ABSOLUTE;
        return root;
    }

    public static TreeReference selfRef () {
        TreeReference self = new TreeReference();
        self.refLevel = 0;
        self.contextType = CONTEXT_INHERITED;
        return self;
    }

    public TreeReference () {
        instanceName = null; // null means the default instance
        refLevel = 0;
        contextType = CONTEXT_ABSOLUTE;
        data = new ArrayList<TreeReferenceLevel>(0);
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public int getMultiplicity(int index) {
        return data.get(index).getMultiplicity();
    }

    public String getName(int index) {
        return data.get(index).getName();
    }

    public int getMultLast () {
        return data.get(data.size() - 1).getMultiplicity();
    }

    public String getNameLast () {
        return data.get(data.size() -1 ).getName();
    }

    public void setMultiplicity (int i, int mult) {
        data.set(i, data.get(i).setMultiplicity(mult));
    }

    public int size () {
        return data.size();
    }

    private void add (TreeReferenceLevel level) {
        data.add(level);
    }

    public void add (String name, int mult) {
        add(new TreeReferenceLevel(name, mult).intern());
    }

    public void addPredicate(int key, List<XPathExpression> xpe)
    {
        data.set(key, data.get(key).setPredicates(xpe));
    }

    public List<XPathExpression> getPredicate(int key)
    {
        return data.get(key).getPredicates();
    }

    public int getRefLevel () {
        return refLevel;
    }

    public void setRefLevel (int refLevel) {
        this.refLevel = refLevel;
    }

    public void incrementRefLevel () {
        if (!isAbsolute()) {
            refLevel++;
        }
    }

    public boolean isAbsolute () {
        return refLevel == REF_ABSOLUTE;
    }

    //return true if this ref contains any unbound multiplicities... ie, there is ANY chance this ref
    //could ambiguously refer to more than one instance node.
    public boolean isAmbiguous () {
        //ignore level 0, as /data implies /data[0]
        for (int i = 1; i < size(); i++) {
            if (getMultiplicity(i) == INDEX_UNBOUND) {
                return true;
            }
        }
        return false;
    }

    //return a copy of the ref
    @Override
    public TreeReference clone () {
        TreeReference newRef = new TreeReference();
        newRef.setRefLevel(this.refLevel);

        for(TreeReferenceLevel l : data) {
            newRef.add(l.shallowCopy());
        }

        //copy instances
        newRef.setInstanceName(instanceName);
        newRef.setContext(this.contextType);
        return newRef;
    }

    /*
     * chop the lowest level off the ref so that the ref now represents the parent of the original ref
     * return true if we successfully got the parent, false if there were no higher levels
     */
    public boolean removeLastLevel () {
        int size = size();
        if (size == 0) {
            if (isAbsolute()) {
                return false;
            } else {
                refLevel++;
                return true;
            }
        } else {
            data.remove(size - 1);
            return true;
        }
    }

    public TreeReference getParentRef () {
        //TODO: level
        TreeReference ref = this.clone();
        if (ref.removeLastLevel()) {
            return ref;
        } else {
            return null;
        }
    }



    /**
     * Builds and returns a new reference that is this reference anchored to a passed-in base reference.
     *
     * Unlike in {@link #anchor(TreeReference)}, the base reference can be relative. This allows anchoring of nodes
     * with inherited context such as in {@link org.javarosa.core.model.instance.TreeElement#BuildRef} which
     * {@link #anchor(TreeReference) anchor} can't do. However, if this ref has 'parent' steps (..), it can only be
     * anchored if the base ref is a relative ref consisting only of other 'parent' steps. For example,
     * '../../a'.parent('..') is valid and yields '../../../a'.
     *
     * @return a new TreeReference that represents this reference anchored to the parent or null if this ref has parent
     * steps and the parent ref is not a relative reference consisting only of other 'parent' steps.
     **/
    public TreeReference parent(TreeReference baseReference) {
        if (isAbsolute()) {
            return this.clone();
        } else {
            TreeReference newRef = baseReference.clone();

            if (refLevel > 0) {
                if (!baseReference.isAbsolute() && baseReference.size() == 0) {
                    newRef.refLevel += refLevel;
                } else {
                    return null;
                }
            }

            for (TreeReferenceLevel level : data) {
                newRef.add(level.shallowCopy());
            }

            return newRef;
        }
    }

    /**
     * Builds and returns a new reference that is this reference anchored to a passed-in base reference.
     *
     * Unlike in {@link #parent(TreeReference)}, the base reference must be absolute and refer to a singular, existing
     * node in the model. For example, this allows '../../d/e/f'.anchor('/a/b/c') -> '/a/d/e/f' which can't be done by
     * {@link #parent(TreeReference)}.
     *
     * NOTE: this function still works even when baseReference contains INDEX_UNBOUND multiplicities. Conditions depend
     * on this behavior, even though it's slightly icky.
     *
     * TODO: Technically we should possibly be modifying context stuff here instead of in the xpath stuff
     *
     * @return a new TreeReference that represents this reference anchored to the context.
     * @throws XPathException if the context reference is not absolute or if there is an attempt to parent past the
     * root node (too many '../' levels).
     */
    public TreeReference anchor (TreeReference baseReference) throws XPathException {
        if (isAbsolute()) {
            return this.clone();
        } else if (!baseReference.isAbsolute()) {
            throw new XPathException(baseReference.toString(true) + " is not an absolute reference");
        } else if (refLevel > baseReference.size()) {
            throw new XPathException("Attempt to parent past the root node " + this.toString(true));
        }

        TreeReference newRef = baseReference.clone();

        for (int i = 0; i < refLevel; i++) {
            newRef.removeLastLevel();
        }

        for (TreeReferenceLevel level : data) {
            newRef.add(level.shallowCopy());
        }

        return newRef;
    }

    public TreeReference contextualize (TreeReference contextRef) {
        //TODO: Technically we should possibly be modifying context stuff here
        //instead of in the xpath stuff;
        if (!contextRef.isAbsolute()){
            return null;
        }

        // I think contextualizing of absolute nodes still needs to be done.
        // They may contain predicates that need to be contextualized.

        TreeReference newRef = anchor(contextRef);
        // unclear...
        newRef.setContext(contextRef.getContext());

        //apply multiplicities and fill in wildcards as necessary based on the context ref
        for (int i = 0; i < contextRef.size() && i < newRef.size(); i++) {

            //If the the contextRef can provide a definition for a wildcard, do so
            if(TreeReference.NAME_WILDCARD.equals(newRef.getName(i)) && !TreeReference.NAME_WILDCARD.equals(contextRef.getName(i))) {
                newRef.data.set(i, newRef.data.get(i).setName(contextRef.getName(i)));
            }

            if (contextRef.getName(i).equals(newRef.getName(i))) {
                //We can't actually merge nodes if the newRef has predicates or filters
                //on this expression, since those reset any existing resolutions which
                //may have been done.
                if(newRef.getPredicate(i) == null) {
                    newRef.setMultiplicity(i, contextRef.getMultiplicity(i));
                    newRef.addPredicate(i, contextRef.getPredicate(i));
                }
            } else {
                break;
            }
        }

        return newRef;
    }

    public TreeReference relativize (TreeReference parent) {
        if (parent.isAncestorOf(this, false)) {
            TreeReference relRef = selfRef();
            for (int i = parent.size(); i < this.size(); i++) {
                relRef.add(this.getName(i), INDEX_UNBOUND);
            }
            return relRef;
        } else {
            return null;
        }
    }

    //turn unambiguous ref into a generic ref
    public TreeReference genericize () {
        TreeReference genericRef = clone();
        for (int i = 0; i < genericRef.size(); i++) {
            //TODO: It's not super clear whether template refs should get
            //genericized or not
            genericRef.setMultiplicity(i, INDEX_UNBOUND);
        }
        return genericRef;
    }

    //returns true if 'this' is parent of 'child'
    //return true if 'this' equals 'child' only if properParent is false
    public boolean isAncestorOf(TreeReference child, boolean properParent) {
        //Instances and context types;
        if (refLevel != child.refLevel)
            return false;
        if (child.size() < size() + (properParent ? 1 : 0))
            return false;

        for (int i = 0; i < size(); i++) {
            if (!this.getName(i).equals(child.getName(i))) {
                return false;
            }

            int parMult = this.getMultiplicity(i);
            int childMult = child.getMultiplicity(i);
            if (parMult != INDEX_UNBOUND && parMult != childMult && !(i == 0 && parMult == 0 && childMult == INDEX_UNBOUND)) {
                return false;
            }
        }

        return true;
    }

    /**
     * clone and extend a reference by one level
     * @param name
     * @param mult
     * @return
     */
    public TreeReference extendRef (String name, int mult) {
        //TODO: Shouldn't work for this if this is an attribute ref;
        TreeReference childRef = this.clone();
        childRef.add(name, mult);
        return childRef;
    }

    public boolean equals (Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof TreeReference) {
            TreeReference ref = (TreeReference)o;

            if (this.refLevel == ref.refLevel && this.size() == ref.size()) {
                for (int i = 0; i < this.size(); i++) {
                    String nameA = this.getName(i);
                    String nameB = ref.getName(i);
                    int multA = this.getMultiplicity(i);
                    int multB = ref.getMultiplicity(i);

                    List<XPathExpression> predA = this.getPredicate(i);
                    List<XPathExpression> predB = ref.getPredicate(i);

                    if (!nameA.equals(nameB)) {
                        return false;
                    } else if (multA != multB) {
                        if (i == 0 && (multA == 0 || multA == INDEX_UNBOUND) && (multB == 0 || multB == INDEX_UNBOUND)) {
                            // /data and /data[0] are functionally the same
                        } else {
                            return false;
                        }
                    } else if(predA != null && predB != null) {
                        if(predA.size() != predB.size()) { return false;}
                        for(int j = 0 ; j < predA.size() ; ++j) {
                            if(!predA.get(j).equals(predB.get(j))) {
                                return false;
                            }
                        }
                    } else if((predA == null && predB != null) || (predA != null && predB == null)){
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int hashCode () {
        int hash = (Integer.valueOf(refLevel)).hashCode();
        for (int i = 0; i < size(); i++) {
            //NOTE(ctsims): It looks like this is only using Integer to
            //get the hashcode method, but that method
            //is just returning the int value, I think, so
            //this should potentially just be replaced by
            //an int.
            Integer mult = DataUtil.integer(getMultiplicity(i));
            if (i == 0 && mult.intValue() == INDEX_UNBOUND)
                mult = DataUtil.integer(0);

            hash ^= getName(i).hashCode();
            hash ^= mult.hashCode();
         List<XPathExpression> predicates = this.getPredicate(i);
            if(predicates == null) {
                continue;
            }
            int val = 0;
            for(XPathExpression xpe : predicates) {
                hash ^= val;
                hash ^= xpe.hashCode();
                ++val;
            }
        }
        return hash;
    }

    public String toString () {
        return toString(true);
    }

    public String toString (boolean includePredicates) {
        return toString(includePredicates, false);
    }

    public String toString (boolean includePredicates, boolean zeroIndexMult) {
        StringBuilder sb = new StringBuilder();
        if(instanceName != null) {
            sb.append("instance("+instanceName+")");
        } else if(contextType == CONTEXT_ORIGINAL) {
            sb.append("current()");
        } else if(contextType == CONTEXT_INHERITED) {
            sb.append("inherited()");
        }
        if (isAbsolute()) {
            sb.append("/");
        } else {
            for (int i = 0; i < refLevel; i++)
                sb.append("../");
        }
        for (int i = 0; i < size(); i++) {
            String name = getName(i);
            int mult = getMultiplicity(i);

            if(mult == INDEX_ATTRIBUTE) {
                sb.append("@");
            }
            sb.append(name);

            if (includePredicates) {
                switch (mult) {
                    case INDEX_UNBOUND: break;
                    case INDEX_TEMPLATE: sb.append("[@template]"); break;
                    case INDEX_REPEAT_JUNCTURE: sb.append("[@juncture]"); break;
                    default:
                        if ((i > 0 || mult != 0) && mult !=-4) {
                            sb.append("[").append(mult + (zeroIndexMult ? 0 : 1)).append("]");
                        }
                        break;
                }
            }

            if (i < size() - 1)
                sb.append("/");
        }
        return sb.toString();
    }

    //For debugging purposes only.
    public String toShortString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size(); i++) {
            int mult = getMultiplicity(i);

            switch (mult) {
                case INDEX_UNBOUND: break;
                case INDEX_TEMPLATE: sb.append("[@template]"); break;
                case INDEX_REPEAT_JUNCTURE: sb.append("[@juncture]"); break;
                default:
                    if ((i > 0 || mult != 0) && mult !=-4) {
                        if (sb.length() > 0) {
                            sb.append("_");
                        }
                        sb.append(mult + 1);
                    }
                    break;
            }
        }

        return getNameLast() + " [" + sb.toString() + "]";
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        refLevel = ExtUtil.readInt(in);
        instanceName = (String)ExtUtil.read(in, new ExtWrapNullable(String.class),pf);
        contextType = ExtUtil.readInt(in);
        int size = ExtUtil.readInt(in);
        for(int i = 0 ; i < size; ++i) {
            TreeReferenceLevel level = (TreeReferenceLevel)ExtUtil.read(in, TreeReferenceLevel.class);
            this.add(level.intern());
        }
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, refLevel);
        ExtUtil.write(out, new ExtWrapNullable(instanceName));
        ExtUtil.writeNumeric(out, contextType);
        ExtUtil.writeNumeric(out, size());
        for(TreeReferenceLevel l : data) {
            ExtUtil.write(out, l);
        }
    }

    /** Intersect this tree reference with another, returning a new tree reference
     *  which contains all of the common elements, starting with the root element.
     *
     *  Note that relative references by their nature can't share steps, so intersecting
     *  any (or by any) relative ref will result in the root ref. Additionally, if the
     *  two references don't share any steps, the intersection will consist of the root
     *  reference.
     *
     * @param b The tree reference to intersect
     * @return The tree reference containing the common basis of this ref and b
     */
    public TreeReference intersect(TreeReference b) {
        if(!this.isAbsolute() || !b.isAbsolute()) {
            return TreeReference.rootRef();
        }
        if(this.equals(b)) { return this;}


        TreeReference a;
        //A should always be bigger if one ref is larger than the other
        if(this.size() < b.size()) { a = b.clone() ; b = this.clone();}
        else { a= this.clone(); b = b.clone();}

        //Now, trim the refs to the same length.
        int diff = a.size() - b.size();
        for(int i = 0; i < diff; ++i) {
            a.removeLastLevel();
        }

        int aSize = a.size();
        //easy, but requires a lot of re-evaluation.
        for(int i = 0 ; i <=  aSize; ++i) {
            if(a.equals(b)) {
                return a;
            } else if(a.size() == 0) {
                return TreeReference.rootRef();
            } else {
                if(!a.removeLastLevel() || !b.removeLastLevel()) {
                    //I don't think it should be possible for us to get here, so flip if we do
                    throw new RuntimeException("Dug too deply into TreeReference during intersection");
                }
            }
        }

        //The only way to get here is if a's size is -1
        throw new RuntimeException("Impossible state");
    }

    //TODO: This should be in construction
    public void setContext(int context) {
        this.contextType = context;
    }

    public int getContext() {
        return this.contextType;
    }

    /**
     * Returns the subreference of this reference up to the level specified.
     *
     * Used to identify the reference context for a predicate at the same level
     *
     * Must be an absolute reference, otherwise will throw IllegalArgumentException
     *
     * @param level
     * @return
     */
    public TreeReference getSubReference(int level) {
        if(!this.isAbsolute()) { throw new IllegalArgumentException("Cannot subreference a non-absolute ref"); }

        //Copy construct
        TreeReference ret = new TreeReference();
        ret.refLevel = this.refLevel;
        ret.contextType = this.contextType;
        ret.instanceName = this.instanceName;
        ret.data = new ArrayList<TreeReferenceLevel>(level);
        for(int i = 0 ; i <= level ; ++i) {
            ret.data.add(this.data.get(i));
        }
        return ret;
    }

    public boolean hasPredicates() {
        for(TreeReferenceLevel level : data) {
            if(level.getPredicates() != null) {
                return true;
            }
        }
        return false;
    }

    public TreeReference removePredicates() {
        TreeReference predicateless = clone();
        for(int i = 0; i < predicateless.data.size(); ++i) {
            predicateless.data.set(i, predicateless.data.get(i).setPredicates(null));
        }
        return predicateless;
    }
}
