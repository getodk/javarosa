/**
 *
 */
package org.javarosa.core.model.data;

import org.javarosa.core.model.Constants;

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
	 *
	 * @param controlType
	 * @param dataType
	 * @return
	 */
	public static IAnswerData template(int controlType, int datatype) {
		//First take care of the easy two, selections, since their
		//datatype is implicit
		if(controlType == Constants.CONTROL_SELECT_ONE) {
			return new SelectOneData();
		}

		if(controlType == Constants.CONTROL_SELECT_MULTI) {
			return new SelectMultiData();
		}

		//That's actually it for now, we might have more in the future
		//so now return the template based on just data
		return templateByDataType(datatype);
	}

	public static IAnswerData templateByDataType(int datatype) {
		switch(datatype) {
			case Constants.DATATYPE_CHOICE:
				return new SelectOneData();
			case Constants.DATATYPE_CHOICE_LIST:
				return new SelectMultiData();
			case Constants.DATATYPE_BOOLEAN:
				return new BooleanData();
			case Constants.DATATYPE_DATE:
				return new DateData();
			case Constants.DATATYPE_DATE_TIME:
				return new DateTimeData();
			case Constants.DATATYPE_DECIMAL:
				return new DecimalData();
			case Constants.DATATYPE_GEOPOINT:
				return new GeoPointData();
			case Constants.DATATYPE_GEOSHAPE:
				return new GeoShapeData();
			case Constants.DATATYPE_GEOTRACE:
				return new GeoTraceData();
			case Constants.DATATYPE_INTEGER:
				return new IntegerData();
			case Constants.DATATYPE_LONG:
				return new LongData();
			case Constants.DATATYPE_TEXT:
				return new StringData();
			case Constants.DATATYPE_TIME:
				return new TimeData();

			//All of these are things that might require other manipulations in the future, but
			//for low can all just live as untyped
			case Constants.DATATYPE_BARCODE:
			case Constants.DATATYPE_BINARY:
			case Constants.DATATYPE_UNSUPPORTED:
			case Constants.DATATYPE_NULL:
				return new UncastData();

			//If this is new and we don't know what's going on, just leave it untyped
			default:
				return new UncastData();
		}
	}
}
