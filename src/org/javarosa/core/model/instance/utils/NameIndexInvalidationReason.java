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

/** {@link NameIndex}es will become invalid under these conditions */
enum NameIndexInvalidationReason {
    MULT_TYPE_NOT_SUPPORTED ("Special multiplicity types other than INDEX_TEMPLATE are not supported"),
    SECOND_TEMPLATE         ("Was requested to add a second template"),
    TEMPLATE_AFTER_OTHER    ("Was requested to add a template after other elements"),
    ORDER                   ("Out of order multiplicity"),
    ELEMENT_REMOVED         ("Element removed"),
    DECREMENT_BELOW_ZERO    ("Attempted to decrement size below zero"),
    INDEX_SHIFT_BELOW_ZERO  ("Attempted to shift index below zero");
    private final String explanation;

    NameIndexInvalidationReason(String explanation) {
        this.explanation = explanation;
    }

    @Override public String toString() {
        return explanation;
    }
}
