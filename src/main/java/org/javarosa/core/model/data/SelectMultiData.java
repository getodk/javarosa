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

package org.javarosa.core.model.data;

import org.javarosa.core.model.data.helper.Selection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class is only for providing backwards compatibility after renaming SelectMultiData.class to MultipleItemsData
 */
public class SelectMultiData extends MultipleItemsData {

    /**
     * Empty Constructor, necessary for dynamic construction during deserialization. Shouldn't be used otherwise.
     */
    public SelectMultiData() {

    }

    public SelectMultiData(@NotNull List<Selection> vs) {
        setValue(vs);
    }
}
