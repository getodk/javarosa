/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model.instance.utils;

import org.javarosa.core.io.Std;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.DECREMENT_BELOW_ZERO;
import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.INDEX_SHIFT_BELOW_ZERO;
import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.MULT_TYPE_NOT_SUPPORTED;
import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.ORDER;
import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.SECOND_TEMPLATE;
import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.TEMPLATE_AFTER_OTHER;

/**
 * An index for a group of {@link TreeElement}s that all have the same name, used to
 * provide direct access within a list of children to a specific name and multiplicity value.
 * Before using a NameIndex, call isValid to see if the index is valid for the pattern
 * of children present. If it isn’t, use sequential search methods.
 */
class NameIndex {
    private int numTemplates;
    private Integer sequenceStartIndex;
    private int sequenceLength;
    private NameIndexInvalidationReason invalidReason;
    private Boolean valid;
    public static boolean logWhenSetInvalid = true;

    /** Returns whether this index is valid */
    public Boolean isValid() {
        return valid;
    }

    /**
     * Returns the size of the index, including the template, if requested
     *
     * @param includeTemplate whether to include any template, if present
     **/
    public int size(boolean includeTemplate) {
        return sequenceLength - (includeTemplate ? 0 : numTemplates);
    }

    /** Adds an element to the index */
    void add(int mult, int index) {
        if (valid != null && !valid) {
            // Ignore request because this index is invalid
            return;
        }
        if (mult < 0 && mult != TreeReference.INDEX_TEMPLATE) {
            setInvalid(MULT_TYPE_NOT_SUPPORTED);
            return;
        }
        if (mult == TreeReference.INDEX_TEMPLATE) {
            if (numTemplates > 0) {
                setInvalid(SECOND_TEMPLATE);
                return;
            }
            if (sequenceLength > 0) {
                setInvalid(TEMPLATE_AFTER_OTHER);
                return;
            }
            ++numTemplates;
            sequenceStartIndex = index;
            valid = true;
        } else {
            if (mult == 0 && sequenceStartIndex == null) {
                sequenceStartIndex = index;
            } else if (mult != sequenceLength - numTemplates) {
                setInvalid(ORDER);
                return;
            }
        }
        ++sequenceLength;
        valid = true;
    }

    /** Sets this index invalid and records the reason */
    void setInvalid(NameIndexInvalidationReason reason) {
        valid = false;
        invalidReason = reason;
        if (logWhenSetInvalid) {
            Std.out.println("Index marked invalid: " + reason);
        }
    }

    /** Returns the number of children in the index, not counting any template */
    int numNonTemplateChildren() {
        if (!valid) {
            throw new IllegalStateException("Index is not valid: " + invalidReason);
        }
        return sequenceLength - numTemplates;
    }

    /**
     * Returns the starting index (within all the children) of this index, including the template if requested
     *
     * @param includeTemplate if true, includes the template
     **/
    int startingIndex(boolean includeTemplate) {
        if (!valid) {
            throw new IllegalStateException("Index is not valid: " + invalidReason);
        }
        return sequenceStartIndex + (includeTemplate ? 0 : numTemplates);
    }

    /** Reduces the size of the index by one (following deleting a child) */
    public void shrinkByOne() {
        if (valid) {
            if (sequenceLength > 0) {
                --sequenceLength;
            } else {
                setInvalid(DECREMENT_BELOW_ZERO);
            }
        }
    }

    /**
     * Shifts the sequence start index. Since NameIndex is overlaid on a List of children, when children are
     * added to or removed from that List, the index must adjust accordingly.
     **/
    public void shiftIndex(int amount) {
        if (valid) {
            if (sequenceStartIndex + amount >= 0) {
                sequenceStartIndex += amount;
            } else {
                setInvalid(INDEX_SHIFT_BELOW_ZERO);
            }
        }
    }

    /**
     * Returns the position within the list of children, of the child with
     * the specified multiplicity, or null if no such child is found
     *
     * @param multiplicity the multiplicity of the child
     * @return the position of the child
     */
    public Integer indexOf(int multiplicity) {
        if (!valid) {
            throw new IllegalStateException("Index is not valid: " + invalidReason);
        }
        if (multiplicity == TreeReference.INDEX_TEMPLATE) {
            if (numTemplates == 0) {
                return null;
            }
            return sequenceStartIndex;
        }
        if (multiplicity >= sequenceLength - numTemplates) {
            return null; // Doesn’t exist
        }
        return numTemplates + sequenceStartIndex + multiplicity;
    }

    @Override public String toString() {
        return "NameIndex{" +
                "numTemplates=" + numTemplates +
                ", sequenceStartIndex=" + sequenceStartIndex +
                ", sequenceLength=" + sequenceLength +
                ", invalidReason=" + invalidReason +
                ", valid=" + valid +
                '}';
    }
}
