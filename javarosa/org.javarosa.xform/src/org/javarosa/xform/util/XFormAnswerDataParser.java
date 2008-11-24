package org.javarosa.xform.util;

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.Map;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Element;

/**
 * The XFormAnswerDataParser is responsible for taking XForms elements and
 * parsing them into a specific type of IAnswerData.
 * 
 * @author Clayton Sims
 * 
 */
public class XFormAnswerDataParser {
	Map parsers;

	public static IAnswerData getAnswerData(Vector formElements,
			DataBinding binding, Element node) {
		// TODO: This should be a set of Handlers, not a switch
		String value = XFormParser.getXMLText(node, false);
		if (value == null)
			return null;

		int dataType = 0;
		if (binding == null) {
			dataType = ((QuestionDef) formElements.elementAt(0)).getDataType();
		} else {
			dataType = binding.getDataType();
		}

		switch (dataType) {
		case Constants.DATATYPE_DATE:
			return value.trim().length() == 0 ? null : new DateData(DateUtils
					.getDateFromString(value));
		case Constants.DATATYPE_DATE_TIME:
			// We need to get datetime here, not date
			return value.trim().length() == 0 ? null : new DateData(DateUtils
					.getDateTimeFromString(value));
		case Constants.DATATYPE_INTEGER:
			// return value.trim().length() == 0 ? null : new
			// IntegerData(Integer.parseInt(value));
			return new StringData(value);
		case Constants.DATATYPE_TEXT:
			if (formElements.isEmpty()) {
				return new StringData(value);
			} else {
				QuestionDef questionDef = (QuestionDef) formElements.elementAt(0); // TODO (JMT) this cast is not good
				if (questionDef.getSelectItemIDs() == null) {
					return new StringData(value);
				} else {
					int controlType = questionDef.getControlType();
					switch (controlType) {
					case Constants.CONTROL_SELECT_ONE:
						Selection selection = getSelection(value, formElements);
						return new SelectOneData(selection);
					case Constants.CONTROL_SELECT_MULTI:
						String[] splitValues = split(value, XFormAnswerDataSerializer.DELIMITER);
						Vector selections = new Vector();
						for (int i = 0; i < splitValues.length; i++) {
							selection = getSelection(splitValues[i], formElements);
							if(selection != null){
								selections.addElement(selection);
							}
						}
						return new SelectMultiData(selections);
					}
				}
			}
		case Constants.DATATYPE_TIME:
			// Come up with a parser for this.
			return null;

		}
		return null;
	}

	private static Selection getSelection(String value, Vector formElements) {
		IFormElement element;
		QuestionDef questionDef;
		for (int i = 0; i < formElements.size(); i++) {
			element = (IFormElement) formElements.elementAt(i);
			if(element instanceof QuestionDef){
				questionDef = (QuestionDef) element;
				questionDef.localizeSelectMap(null);
				int index = questionDef.getSelectedItemIndex(value); 
				if(index != -1){
					return new Selection(index, questionDef);
				}
			}
		}
		return null;
	}

	// TODO (JMT) this methods is an util method, put it in a util class
	public static String[] split(String original, String delimiter) {
		Vector nodes = new Vector();
		// Parse nodes into vector
		int index = original.indexOf(delimiter);
		while (index >= 0) {
			nodes.addElement(original.substring(0, index));
			original = original.substring(index + delimiter.length());
			index = original.indexOf(delimiter);
		}
		// Get the last node
		nodes.addElement(original);

		// Create splitted string array
		String[] result = new String[nodes.size()];
		if (nodes.size() > 0) {
			for (int loop = 0; loop < nodes.size(); loop++) {
				result[loop] = (String) nodes.elementAt(loop);
			}

		}
		return result;
	}
}
