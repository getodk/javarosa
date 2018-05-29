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

import static org.javarosa.core.model.Constants.DATATYPE_BARCODE;
import static org.javarosa.core.model.Constants.DATATYPE_BINARY;
import static org.javarosa.core.model.Constants.DATATYPE_BOOLEAN;
import static org.javarosa.core.model.Constants.DATATYPE_CHOICE;
import static org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST;
import static org.javarosa.core.model.Constants.DATATYPE_MULTIPLE_ITEMS;
import static org.javarosa.core.model.Constants.DATATYPE_DATE;
import static org.javarosa.core.model.Constants.DATATYPE_DATE_TIME;
import static org.javarosa.core.model.Constants.DATATYPE_DECIMAL;
import static org.javarosa.core.model.Constants.DATATYPE_GEOPOINT;
import static org.javarosa.core.model.Constants.DATATYPE_GEOSHAPE;
import static org.javarosa.core.model.Constants.DATATYPE_GEOTRACE;
import static org.javarosa.core.model.Constants.DATATYPE_INTEGER;
import static org.javarosa.core.model.Constants.DATATYPE_LONG;
import static org.javarosa.core.model.Constants.DATATYPE_NULL;
import static org.javarosa.core.model.Constants.DATATYPE_TEXT;
import static org.javarosa.core.model.Constants.DATATYPE_TIME;
import static org.javarosa.core.model.Constants.DATATYPE_UNSUPPORTED;

/**
 * The model data types.
 */
public enum DataType {
    UNSUPPORTED     (DATATYPE_UNSUPPORTED),
    NULL            (DATATYPE_NULL),
    TEXT            (DATATYPE_TEXT),
    INTEGER         (DATATYPE_INTEGER),
    DECIMAL         (DATATYPE_DECIMAL),
    DATE            (DATATYPE_DATE),
    TIME            (DATATYPE_TIME),
    DATE_TIME       (DATATYPE_DATE_TIME),
    CHOICE          (DATATYPE_CHOICE),
    MULTIPLE_ITEMS  (DATATYPE_MULTIPLE_ITEMS),
    BOOLEAN         (DATATYPE_BOOLEAN),
    GEOPOINT        (DATATYPE_GEOPOINT),
    BARCODE         (DATATYPE_BARCODE),
    BINARY          (DATATYPE_BINARY),
    LONG            (DATATYPE_LONG),
    GEOSHAPE        (DATATYPE_GEOSHAPE),
    GEOTRACE        (DATATYPE_GEOTRACE);

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
