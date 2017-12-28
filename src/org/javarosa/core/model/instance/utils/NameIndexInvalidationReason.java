package org.javarosa.core.model.instance.utils;

/** {@link NameIndex}es will become invalid under these conditions */
enum NameIndexInvalidationReason {
    MULT_TYPE_NOT_SUPPORTED ("Special multiplicity types other than INDEX_TEMPLATE are not supported"),
    SECOND_TEMPLATE         ("Was requested to add a second template"),
    TEMPLATE_AFTER_OTHER    ("Was requested to add a template after other elements"),
    ORDER                   ("Out of order multiplicity"),
    ELEMENT_REMOVED         ("Element removed"),
    DECREMENT_BELOW_ZERO    ("Attempted to decrement size below zero");
    private final String explanation;

    NameIndexInvalidationReason(String explanation) {
        this.explanation = explanation;
    }

    @Override public String toString() {
        return explanation;
    }
}
