package org.javarosa.core.model.instance.utils;

import org.javarosa.core.io.Std;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

import static org.javarosa.core.model.instance.utils.NameIndexInvalidationReason.*;

/**
 * An index for a group of {@link TreeElement}s that all have the same name, used to
 * provide direct access within a list of children to a specific name and multiplicity value.
 * Before using a NameIndex, call isValid to see if the index is valid for the pattern
 * of children present. If it isn’t, use sequential search methods.
 */
class NameIndex {
    private int numTemplates;
    private Integer sequenceStartIndexIncludingAnyTemplate;
    private int sequenceLengthIncludingTemplateIfPresent;
    private NameIndexInvalidationReason invalidReason;
    private Boolean valid;
    public static boolean logWhenSetInvalid = true;

    /** Returns whether this index is valid */
    public Boolean isValid() {
        return valid;
    }

    /** Returns the size of the index, not counting a template, if present */
    public int sizeWithoutTemplate() {
        return sequenceLengthIncludingTemplateIfPresent - numTemplates;
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
            if (sequenceLengthIncludingTemplateIfPresent > 0) {
                setInvalid(TEMPLATE_AFTER_OTHER);
                return;
            }
            ++numTemplates;
            sequenceStartIndexIncludingAnyTemplate = index;
            valid = true;
        } else {
            if (mult == 0 && sequenceStartIndexIncludingAnyTemplate == null) {
                sequenceStartIndexIncludingAnyTemplate = index;
            } else if (mult != sequenceLengthIncludingTemplateIfPresent - numTemplates) {
                setInvalid(ORDER);
                return;
            }
        }
        ++sequenceLengthIncludingTemplateIfPresent;
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
        return sequenceLengthIncludingTemplateIfPresent - numTemplates;
    }

    /** Returns the starting index (within all the children) of this index, skipping any template */
    int startingIndexWithoutTemplate() {
        if (!valid) {
            throw new IllegalStateException("Index is not valid: " + invalidReason);
        }
        return sequenceStartIndexIncludingAnyTemplate + numTemplates;
    }

    /** Reduces the size of the index by one (following deleting a child) */
    public void shrinkByOne() {
        if (valid) {
            if (sequenceLengthIncludingTemplateIfPresent > 0) {
                --sequenceLengthIncludingTemplateIfPresent;
            } else {
                setInvalid(DECREMENT_BELOW_ZERO);
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
            return sequenceStartIndexIncludingAnyTemplate;
        }
        if (multiplicity >= sequenceLengthIncludingTemplateIfPresent - numTemplates) {
            return null; // Doesn’t exist
        }
        return numTemplates + sequenceStartIndexIncludingAnyTemplate + multiplicity;
    }
}
