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

package org.javarosa.xform.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.javarosa.core.model.DataType;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoTraceData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The XFormAnswerDataParser is responsible for taking XForms elements and
 * parsing them into a specific type of IAnswerData.
 *
 * @author Clayton Sims
 */

public class XFormAnswerDataParser {
    //FIXME: the QuestionDef parameter is a hack until we find a better way to represent AnswerDatas for select questions

    private static final Logger logger = LoggerFactory.getLogger(XFormAnswerDataParser.class.getSimpleName());

    public static IAnswerData getAnswerData(String text, int dataType) {
        return getAnswerData(text, dataType, null);
    }

    public static IAnswerData getAnswerData(String text, int intDataType, QuestionDef q) {
        String trimmedText = text.trim();
        if (trimmedText.length() == 0)
            trimmedText = null;

        switch (DataType.from(intDataType)) {
            case NULL:
            case UNSUPPORTED:
            case TEXT:
            case BARCODE:
            case BINARY:
                return new StringData(text);

            case INTEGER:
                try {
                    return trimmedText == null ? null : new IntegerData(Integer.parseInt(trimmedText));
                } catch (NumberFormatException nfe) {
                    return null;
                }

            case LONG:
                try {
                    return trimmedText == null ? null : new LongData(Long.parseLong(trimmedText));
                } catch (NumberFormatException nfe) {
                    return null;
                }

            case DECIMAL:
                try {
                    return trimmedText == null ? null : new DecimalData(Double.parseDouble(trimmedText));
                } catch (NumberFormatException nfe) {
                    return null;
                }

            case CHOICE:
                Selection selection = getSelection(text, q);
                return selection == null ? null : new SelectOneData(selection);

            case MULTIPLE_ITEMS:
                return new MultipleItemsData(getSelections(text, q));

            case DATE_TIME:
                Date dt = trimmedText == null ? null : DateUtils.parseDateTime(trimmedText);
                return dt == null ? null : new DateTimeData(dt);

            case DATE:
                Date d = trimmedText == null ? null : DateUtils.parseDate(trimmedText);
                return d == null ? null : new DateData(d);

            case TIME:
                Date t = trimmedText == null ? null : DateUtils.parseTime(trimmedText);
                return t == null ? null : new TimeData(t);

            case BOOLEAN:
                if (trimmedText == null) {
                    return null;
                } else {
                    if (trimmedText.equals("1")) {
                        return new BooleanData(true);
                    }
                    if (trimmedText.equals("0")) {
                        return new BooleanData(false);
                    }
                    return trimmedText.equals("t") ? new BooleanData(true) : new BooleanData(false);
                }

            case GEOPOINT:
                if (trimmedText == null) {
                    return new GeoPointData();
                }
                try {
                    return new GeoPointData().cast(new UncastData(trimmedText));
                } catch (Exception e) {
                    logGeoCreateError(GeoPointData.class, trimmedText, e);
                    return null;
                }

            case GEOSHAPE:
                if (trimmedText == null) {
                    return new GeoShapeData();
                }
                try {
                    return new GeoShapeData().cast(new UncastData(trimmedText));
                } catch (Exception e) {
                    logGeoCreateError(GeoShapeData.class, trimmedText, e);
                    return null;
                }

            case GEOTRACE:
                if (trimmedText == null) {
                    return new GeoTraceData();
                }
                try {
                    return new GeoTraceData().cast(new UncastData(trimmedText));
                } catch (Exception e) {
                    logGeoCreateError(GeoTraceData.class, trimmedText, e);
                    return null;
                }

            default:
                return new UncastData(trimmedText);
        }
    }

    private static void logGeoCreateError(Class geoType, String trimmedText, Exception exception) {
        logger.warn("Could not create {} from \"{}\": {}", geoType.getSimpleName(), trimmedText, exception.toString());
    }

    private static List<Selection> getSelections(String text, QuestionDef q) {
        List<String> choices = DateUtils.split(text, XFormAnswerDataSerializer.DELIMITER, true);
        List<Selection> selections = new ArrayList<>(choices.size()); // assume they are all still valid...

        for (String choice : choices) {
            Selection s = getSelection(choice, q);
            if (s != null)
                selections.add(s);
        }

        return selections;
    }

    private static Selection getSelection(String choiceValue, QuestionDef q) {
        final Selection s;

        if (q == null || q.getDynamicChoices() != null) {
            s = new Selection(choiceValue);
        } else {
            SelectChoice choice = q.getChoiceForValue(choiceValue);
            s = (choice != null ? choice.selection() : null);
        }

        return s;
    }
}
