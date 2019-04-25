package org.javarosa.core.model.data;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataType;

/**
 * This is not a factory, actually, since there's no drop-in component model, but
 * it could be in the future. Just wanted a centralized place to dispatch common
 * templating methods.
 *
 * In the future this could be a legitimate factory which is stored in... say...
 * the evaluation context
 *
 * @author ctsims
 *
 */
public class AnswerDataFactory {

    /**
     * The one-template to rule them all. Takes in a control type and a
     * data type and returns the appropriate answer data template with
     * which to cast incoming values.
     *
     * All enormous spaghetti ifs should be replaced with a call to this
     */
    public static IAnswerData template(int controlType, int datatype) {
        //First take care of the easy two, selections, since their
        //datatype is implicit
        if(controlType == Constants.CONTROL_SELECT_ONE) {
            return new SelectOneData();
        }

        if (controlType == Constants.CONTROL_SELECT_MULTI || controlType == Constants.CONTROL_RANK) {
            return new MultipleItemsData();
        }

        //That's actually it for now, we might have more in the future
        //so now return the template based on just data
        return templateByDataType(datatype);
    }

    public static IAnswerData templateByDataType(int datatype) {
        switch(DataType.from(datatype)) {
            case CHOICE:
                return new SelectOneData();
            case MULTIPLE_ITEMS:
                return new MultipleItemsData();
            case BOOLEAN:
                return new BooleanData();
            case DATE:
                return new DateData();
            case DATE_TIME:
                return new DateTimeData();
            case DECIMAL:
                return new DecimalData();
            case GEOPOINT:
                return new GeoPointData();
            case GEOSHAPE:
                return new GeoShapeData();
            case GEOTRACE:
                return new GeoTraceData();
            case INTEGER:
                return new IntegerData();
            case LONG:
                return new LongData();
            case TEXT:
                return new StringData();
            case TIME:
                return new TimeData();

            //All of these are things that might require other manipulations in the future, but
            //for low can all just live as untyped
            case BARCODE:
            case BINARY:
            case UNSUPPORTED:
            case NULL:
                return new UncastData();

            //If this is new and we don't know what's going on, just leave it untyped
            default:
                return new UncastData();
        }
    }
}
