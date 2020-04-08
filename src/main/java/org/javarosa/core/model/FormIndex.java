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

package org.javarosa.core.model;

import jdk.internal.jline.internal.Nullable;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code FormIndex} is an immutable index which is structured to provide quick access to a specific node in a
 * {@link FormDef} (tree of {@link IFormElement} representing a blank form) or a 
 * {@link org.javarosa.core.model.instance.FormInstance FormInstance} (tree of 
 * {@link org.javarosa.core.model.instance.TreeElement TreeElement} representing a form instance that can be filled). 
 * It also includes a {@link TreeReference} representing the XPath path of that node.
 *
 * The XML node referred to by a FormIndex can correspond to either a question or a group. The index is constructed as a
 * linked list of levels.
 *
 * A FormIndex can refer to:
 * - a node at the top level of the form (only {@link #localIndex} is used)
 * - a node nested in a group hierarchy ({@link #localIndex} and {@link #nextLevel} are used)
 * - a node nested in a repeat ({@link #localIndex}, {@link #nextLevel} and {@link #instanceIndex} are all used)
 *
 * When a FormIndex list refers to a nested node, that root FormIndex represents the path root and links to subsequent
 * levels using the {@link #nextLevel} field. The @{link #getReference} method returns the reference of the FormIndex at
 * the end of the list.
 *
 * Consider the following instance from a blank form with {@code friends} as a repeat group and {@code school_info} as a
 * non-repeat group:
 *
 * <pre>{@code
 *  <instance>
 *    <data id="my_form">
 *      <address/>
 *      <dob/>
 *      <school_info>
 *        <favorite_class/>
 *        <favorite_teacher/>
 *      </school_info>
 *      <friends>
 *        <firstname/>
 *      </friends>
 *    </data>
 *  </instance>
 * </pre>}
 *
 * The {@code dob} question's path is {@code /data/dob}. The index that refers to it has a {@code localIndex} of 1.
 *
 * The {@code friends} group's path is {@code /data/friends}. The index that refers to it has a {@code localIndex} of 3.
 *
 * The {@code favorite_teacher} question's path is {@code /data/school_info/favorite_teacher}. The index that refers to
 * it has a {@code localIndex} of 2 which is {@code school_info}'s index at the form root. It also has a
 * {@code nextLevel} set which itself has a {@code localIndex} of 1 because it is the second node in the school_info group.
 *
 * The {@code firstname} question's path is {@code /data/friends[N]/firstname} where @{N} is a 0-based index representing
 * which of potentially several {@code friends} repeat group instances' {@code firstname} is desired (also referred to as
 * multiplicity). Since {@code friends} is a repeat group, the filled instance can contain several instances of the
 * {@code friends} node. The index that refers to it always starts with a {@code localIndex} of 3 which is
 * {@code friends}'s index at the form root. It also has an {@code instanceIndex} which represents which of the
 * potentially several {@code friends} to use. To index a {@code firstname} node, a {@code nextLevel} is added which
 * always has a @{code localIndex} of 0.
 *
 * To go from a {@code FormIndex} to a form element in a blank form, {@link FormDef#getChild(FormIndex)} can be used.
 * To go from a {@code FormIndex} to a node in a filled form, 
 * {@link org.javarosa.core.model.instance.FormInstance#resolveReference(TreeReference)} can
 * be used along with {@link #getReference()}.
 *
 * No circularity is allowed. That is, no {@code FormIndex}'s ancestor can be itself.
 *
 * Datatype Productions:
 * FormIndex = BOF | EOF | CompoundIndex(nextIndex:FormIndex,Location)
 * Location = Empty | Simple(localLevel:int) | WithMult(localLevel:int, multiplicity:int)
 *
 * @author Clayton Sims
 *
 */
public class FormIndex implements Serializable {

    private boolean beginningOfForm = false;

    private boolean endOfForm = false;

    /**
     * The 0-based index of the group or question in its parent.
     */
    private int localIndex;

    /**
     * The 0-based index of the current instance of a repeated node.
     */
    private int instanceIndex = -1;

    /**
     * The next level of this index. A {@code FormIndex} chain starts at the root and is linked to deeper nodes in the
     * tree it indexes into.
     */
    private FormIndex nextLevel;

    /**
     * The XPath reference this index refers to. Warning: these are mutable and could conceivably get out of sync with
     * the index.
     */
    private TreeReference reference;

    /**
     * Returns an index before the start of the form
     */
    public static FormIndex createBeginningOfFormIndex() {
        FormIndex begin = new FormIndex(-1, null);
        begin.beginningOfForm = true;
        return begin;
    }

    /**
     * Returns an index after the end of the form
     */
    public static FormIndex createEndOfFormIndex() {
        FormIndex end = new FormIndex(-1,null);
        end.endOfForm = true;
        return end;
    }

    /**
     * Constructs a @{code FormIndex} for a node that does not have any children.
     *
     * @param localIndex An integer index into a flat list of elements
     * @param reference A reference to the instance element identified by this index
     */
    public FormIndex(int localIndex, TreeReference reference) {
        this.localIndex = localIndex;
        this.reference = reference;

    }
    /**
     * Constructs a @{code FormIndex} for a node that does not have any children but that may have repeat instances in a
     * filled form. The {@code instanceIndex} identifies which of these instances should be referred to.
     *
     * @param localIndex An integer index into a flat list of elements
     * @param instanceIndex An integer index expressing the multiplicity of the current level
     * @param reference A reference to the instance element identified by this index
     *
     */
    public FormIndex(int localIndex, int instanceIndex, TreeReference reference) {
        this.localIndex = localIndex;
        this.instanceIndex = instanceIndex;
        this.reference = reference;
    }

    /**
     * Constructs a @{code FormIndex} for a node that has children.
     *
     * @param nextLevel An index into the referenced element's index
     * @param localIndex An index to an element at the current level, a child
     * element of which will be referenced by the nextLevel index.
     * @param reference A reference to the instance element identified by this index;
     */
    public FormIndex(FormIndex nextLevel, int localIndex, TreeReference reference) {
        this(localIndex, reference);
        this.nextLevel = nextLevel;
    }

    /**
     * Constructs a @{code FormIndex} for a node that has children and that may have repeat instances in a filled form.
     *
     * @param nextLevel An index into the referenced element's index
     * @param localIndex An index to an element at the current level, a child
     * element of which will be referenced by the nextLevel index.
     * @param instanceIndex How many times the element referenced has been
     * repeated.
     * @param reference A reference to the instance element identified by this index;
     */
    public FormIndex(FormIndex nextLevel, int localIndex, int instanceIndex, TreeReference reference) {
        this(nextLevel, localIndex, reference);
        this.instanceIndex = instanceIndex;
    }

    /**
     * Constructs an index which references an element past the level of
     * specificity of the current context, founded by the currentLevel
     * index.
     * (currentLevel, (nextLevel...))
     */
    public FormIndex(FormIndex nextLevel, FormIndex currentLevel) {
        if(currentLevel == null) {
            this.nextLevel = nextLevel.nextLevel;
            this.localIndex = nextLevel.localIndex;
            this.instanceIndex = nextLevel.instanceIndex;
            this.reference = nextLevel.reference;
        } else {
            this.nextLevel = nextLevel;
            this.localIndex = currentLevel.getLocalIndex();
            this.instanceIndex = currentLevel.getInstanceIndex();
            this.reference = currentLevel.reference;
        }
    }

    /**
     *
     * @return true if the index is neither before the start or after the end of the form
     */
    public boolean isInForm () {
        return !beginningOfForm && !endOfForm;
    }

    /**
     * @return The index of the element in the current context
     */
    public int getLocalIndex() {
        return localIndex;
    }

    /**
     * @return The multiplicity of the current instance of a repeated question or group
     */
    public int getInstanceIndex() {
        return instanceIndex;
    }

    /**
     * For the fully qualified element, get the multiplicity of the element's reference
     * @return The terminal element (fully qualified)'s instance index
     */
    public int getElementMultiplicity() {
        return getTerminal().instanceIndex;
    }

    /**
     * @return An index into the next level of specificity past the current context. An
     * example would be an index  into an element that is a child of the element referenced
     * by the local index.
     */
    public FormIndex getNextLevel() {
        return nextLevel;
    }

    public TreeReference getLocalReference() {
        return reference;
    }

    /**
     * @return The TreeReference of the fully qualified element described by this
     * FormIndex.
     */
    public TreeReference getReference() {
        return getTerminal().reference;
    }

    public FormIndex getTerminal() {
        FormIndex walker = this;
        while(walker.nextLevel != null) {
            walker = walker.nextLevel;
        }
        return walker;
    }

    /**
     * Identifies whether this is a terminal index, in other words whether this
     * index references with more specificity than the current context
     */
    public boolean isTerminal() {
        return nextLevel == null;
    }

    /**
     *
     * @return true if we are after the end of the form
     */
    public boolean isEndOfFormIndex() {
        return endOfForm;
    }

    /**
     *
     * @return true if we are before the start of the form
     */
    public boolean isBeginningOfFormIndex() {
        return beginningOfForm;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof FormIndex))
            return false;

        FormIndex a = this;
        FormIndex b = (FormIndex)o;

        return (a.compareTo(b) == 0);

//        //TODO: while(true) loops freak me out, this should probably
//        //get written more safely. -ctsims
//
//        //Iterate over each level of reference, and identify whether
//        //each object stays in sync
//        while(true) {
//            if(index.isTerminal() != local.isTerminal() ||
//                    index.getLocalIndex() != local.getLocalIndex() ||
//                    index.getInstanceIndex() != local.getInstanceIndex()) {
//                return false;
//            }
//            if(index.isTerminal()) {
//                return true;
//            }
//            local = local.getNextLevel();
//            index = index.getNextLevel();
//        }
//
    }

    @Override
    public int hashCode() {
        // The reference field is not included. This matches the equals(Object) implementation. TreeReferences are
        // mutable and are provided as a way to convert between FormIndex and other types.
        return Objects.hash(beginningOfForm, endOfForm, localIndex, instanceIndex, nextLevel);
    }

    public int compareTo(Object o) {
        if(!(o instanceof FormIndex))
            throw new IllegalArgumentException("Attempt to compare Object of type " + o.getClass().getName() + " to a FormIndex");

        FormIndex a = this;
        FormIndex b = (FormIndex)o;

        if (a.beginningOfForm) {
            return (b.beginningOfForm ? 0 : -1);
        } else if (a.endOfForm) {
            return (b.endOfForm ? 0 : 1);
        } else {
            //a is in form
            if (b.beginningOfForm) {
                return 1;
            } else if (b.endOfForm) {
                return -1;
            }
        }

        if (a.localIndex != b.localIndex) {
            return (a.localIndex < b.localIndex ? -1 : 1);
        } else if (a.instanceIndex != b.instanceIndex) {
            return (a.instanceIndex < b.instanceIndex ? -1 : 1);
        } else if ((a.getNextLevel() == null) != (b.getNextLevel() == null)) {
            return (a.getNextLevel() == null ? -1 : 1);
        } else if (a.getNextLevel() != null) {
            return a.getNextLevel().compareTo(b.getNextLevel());
        } else {
            return 0;
        }

//        int comp = 0;
//
//        //TODO: while(true) loops freak me out, this should probably
//        //get written more safely. -ctsims
//        while(comp == 0) {
//            if(index.isTerminal() != local.isTerminal() ||
//                    index.getLocalIndex() != local.getLocalIndex() ||
//                    index.getInstanceIndex() != local.getInstanceIndex()) {
//                if(local.localIndex > index.localIndex) {
//                    return 1;
//                } else if(local.localIndex < index.localIndex) {
//                    return -1;
//                } else if (local.instanceIndex > index.instanceIndex) {
//                    return 1;
//                } else if (local.instanceIndex < index.instanceIndex) {
//                    return -1;
//                }
//
//                //This case is here as a fallback, but it shouldn't really
//                //ever be the case that two references have the same chain
//                //of indices without terminating at the same level.
//                else if (local.isTerminal() && !index.isTerminal()) {
//                    return -1;
//                } else {
//                    return 1;
//                }
//            }
//            else if(local.isTerminal()) {
//                break;
//            }
//            local = local.getNextLevel();
//            index = index.getNextLevel();
//        }
//        return comp;
    }

    /**
     * @return Only the local component of this Form Index.
     */
    public FormIndex snip() {
        FormIndex retval = new FormIndex(localIndex, instanceIndex,reference);
        return retval;
    }

    /**
     * Takes in a form index which is a subset of this index, and returns the
     * total difference between them. This is useful for stepping up the level
     * of index specificty. If the subIndex is not a valid subIndex of this index,
     * null is returned. Since the FormIndex represented by null is always a subset,
     * if null is passed in as a subIndex, the full index is returned
     *
     * For example:
     * Indices
     * a = 1_0,2,1,3
     * b = 1,3
     *
     * a.diff(b) = 1_0,2
     *
     * @param subIndex
     * @return
     */
    public FormIndex diff(FormIndex subIndex) {
        if(subIndex == null) {
            return this;
        }
        if(!isSubIndex(this,subIndex)) {
            return null;
        }
        if(subIndex.equals(this)) {
            return null;
        }
        return new FormIndex(nextLevel.diff(subIndex),this.snip());
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        FormIndex ref = this;
        while (ref != null) {
            b.append(ref.getLocalIndex());
            if ( ref.getInstanceIndex() != -1) {
                b.append("_").append(ref.getInstanceIndex());
            }
            b.append(", ");
            ref = ref.nextLevel;
        }
        return b.toString();
    }

    /**
     * @return the level of this index relative to the top level of the form
     */
    public int getDepth() {

        int depth = 0;
        FormIndex ref = this;
        while (ref != null) {
            ref = ref.nextLevel;
            depth++;
        }
        return depth;

    }

    /**
     * Trims any negative indices from the end of the passed in index.
     *
     * @param index
     * @return
     */
    public static FormIndex trimNegativeIndices(FormIndex index) {
        if(!index.isTerminal()) {
            return new FormIndex(trimNegativeIndices(index.nextLevel),index);
        } else {
            if(index.getLocalIndex() < 0) {
                return null;
            } else {
                return index;
            }
        }
    }

    public static boolean isSubIndex(FormIndex parent, FormIndex child) {
        if(child.equals(parent)) {
            return true;
        } else {
            if(parent == null) {
                return false;
            }
            return isSubIndex(parent.nextLevel, child);
        }
    }

    public static boolean isSubElement(FormIndex parent, FormIndex child) {
        while(!parent.isTerminal() && !child.isTerminal()) {
            if(parent.getLocalIndex() != child.getLocalIndex()) {
                return false;
            }
            if(parent.getInstanceIndex() != child.getInstanceIndex()) {
                return false;
            }
            parent = parent.nextLevel;
            child = child.nextLevel;
        }
        //If we've gotten this far, at least one of the two is terminal
        if(!parent.isTerminal() && child.isTerminal()) {
            //can't be the parent if the child is earlier on
            return false;
        }
        else if(parent.getLocalIndex() != child.getLocalIndex()) {
            //Either they're at the same level, in which case only
            //identical indices should match, or they should have
            //the same root
            return false;
        }
        else if(parent.getInstanceIndex() != -1 && (parent.getInstanceIndex() != child.getInstanceIndex())) {
            return false;
        }
        //Barring all of these cases, it should be true.
        return true;
    }

    public void assignRefs(FormDef f) {
        FormIndex cur = this;

      List<Integer> indexes = new ArrayList<Integer>();
      List<Integer> multiplicities = new ArrayList<Integer>();
      List<IFormElement> elements = new ArrayList<IFormElement>();
        f.collapseIndex(this, indexes, multiplicities, elements);

      List<Integer> curMults = new ArrayList<Integer>();
      List<IFormElement> curElems = new ArrayList<IFormElement>();

        int i = 0;
        while (cur != null) {
            curMults.add(multiplicities.get(i));
            curElems.add(elements.get(i));

            TreeReference ref = f.getChildInstanceRef(curElems, curMults);
            cur.reference = ref;

            cur = cur.getNextLevel();
            i++;
        }
    }

    public FormIndex getPreviousLevel() {
        if (isTerminal()) {
            return null;
        } else {
            return new FormIndex(nextLevel.getPreviousLevel(), this);
        }
    }
}
