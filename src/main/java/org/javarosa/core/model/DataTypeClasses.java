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

import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.GeoTraceData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.javarosa.core.model.DataType.BOOLEAN;
import static org.javarosa.core.model.DataType.CHOICE;
import static org.javarosa.core.model.DataType.DATE;
import static org.javarosa.core.model.DataType.DATE_TIME;
import static org.javarosa.core.model.DataType.DECIMAL;
import static org.javarosa.core.model.DataType.GEOPOINT;
import static org.javarosa.core.model.DataType.GEOSHAPE;
import static org.javarosa.core.model.DataType.GEOTRACE;
import static org.javarosa.core.model.DataType.INTEGER;
import static org.javarosa.core.model.DataType.LONG;
import static org.javarosa.core.model.DataType.MULTIPLE_ITEMS;
import static org.javarosa.core.model.DataType.NULL;
import static org.javarosa.core.model.DataType.TEXT;
import static org.javarosa.core.model.DataType.TIME;

public class DataTypeClasses {
    private final static Object[][] typesAndClasses = new Object[][]{
        {NULL,              StringData      .class},
        {TEXT,              StringData      .class},
        {INTEGER,           IntegerData     .class},
        {LONG,              LongData        .class},
        {DECIMAL,           DecimalData     .class},
        {BOOLEAN,           BooleanData     .class},
        {DATE,              DateData        .class},
        {TIME,              TimeData        .class},
        {DATE_TIME,         DateTimeData    .class},
        {CHOICE,            SelectOneData   .class},
        {MULTIPLE_ITEMS,    SelectMultiData .class},
        {GEOPOINT,          GeoPointData    .class},
        {GEOSHAPE,          GeoShapeData    .class},
        {GEOTRACE,          GeoTraceData    .class}
    };

    public static Class classForType(DataType dataType) {
        return classesByType.get(dataType);
    }

    private final static Map<DataType, Class> classesByType = createMap();

    private static Map<DataType, Class> createMap() {
        Map<DataType, Class> m = new HashMap<>();
        for (Object[] typeAndClass : typesAndClasses) {
            m.put((DataType) typeAndClass[0], (Class) typeAndClass[1]);
        }
        return Collections.unmodifiableMap(m);
    }
}
