package org.javarosa.xform.util;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.Map;
import org.kxml2.kdom.Element;

/**
 * The XFormAnswerDataParser is responsible for taking XForms
 * elements and parsing them into a specific type of IAnswerData.
 * 
 * @author Clayton Sims
 *
 */
public class XFormAnswerDataParser {
	Map parsers;

	public static IAnswerData getAnswerData(DataBinding binding, Element node) {
		//TODO: This should be a set of Handlers, not a switch
		String value;
		switch (binding.getDataType()) {
			case Constants.DATATYPE_DATE:
				value = (String)node.getChild(0);
				return new DateData(DateUtils.getDateFromString(value));
			case Constants.DATATYPE_DATE_TIME:
				value = (String)node.getChild(0);
				//We need to get datetime here, not date
				return new DateData(DateUtils.getDateFromString(value));
			case Constants.DATATYPE_INTEGER:
				value = (String)node.getChild(0);
				return new IntegerData(Integer.parseInt(value));
			case Constants.DATATYPE_TEXT:
				value = (String)node.getChild(0);
				return new StringData(value);
			case Constants.DATATYPE_TIME:
				//Come up with a parser for this.
				return null;
		}
		return null;
	}
}
