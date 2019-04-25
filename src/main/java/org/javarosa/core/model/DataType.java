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

package org.javarosa.core.model;

/**
 * The model data types.
 */
public enum DataType {
    UNSUPPORTED     (-1),
    NULL            (0),
    TEXT            (1),
    INTEGER         (2),
    DECIMAL         (3),
    DATE            (4),
    TIME            (5),
    DATE_TIME       (6),
    CHOICE          (7),
    MULTIPLE_ITEMS  (8),
    BOOLEAN         (9),
    GEOPOINT        (10),
    BARCODE         (11),
    BINARY          (12),
    LONG            (13),
    GEOSHAPE        (14),
    GEOTRACE        (15);

    public final int value;

    DataType(int value) {
        this.value = value;
    }

    /**
     * Returns a {@link DataType} from its int value
     *
     * @param intDataType the int value of the requested DataType
     * @return the related {@link DataType} instance
     */
    public static DataType from(int intDataType) {
        for (DataType dt : values()) {
            if (dt.value == intDataType)
                return dt;
        }
        throw new IllegalArgumentException("No DataType with value " + intDataType);
    }
}
