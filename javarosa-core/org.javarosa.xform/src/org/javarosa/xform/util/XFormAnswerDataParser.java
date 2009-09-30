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

import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.utils.DateUtils;

/**
 * The XFormAnswerDataParser is responsible for taking XForms elements and
 * parsing them into a specific type of IAnswerData.
 * 
 * @author Clayton Sims
 * 
 */

/*
int
text
float
datetime
date
time
choice
choice list
*/

public class XFormAnswerDataParser {
	//FIXME: the QuestionDef parameter is a hack until we find a better way to represent AnswerDatas for select questions
	
	public static IAnswerData getAnswerData (String text, int dataType) {
		return getAnswerData(text, dataType, null);
	}
	public static IAnswerData getAnswerData (String text, int dataType, QuestionDef q) {
		String trimmedText = text.trim();
		if (trimmedText.length() == 0)
			trimmedText = null;
		
		switch (dataType) {
		case Constants.DATATYPE_NULL:
		case Constants.DATATYPE_UNSUPPORTED:
		case Constants.DATATYPE_TEXT:

			return new StringData(text);
		
		case Constants.DATATYPE_BARCODE:

			return new StringData(text);

		case Constants.DATATYPE_INTEGER:

			try {
				return (trimmedText == null ? null : new IntegerData(Integer.parseInt(trimmedText)));
			} catch (NumberFormatException nfe) {
				return null;
			}
			
		case Constants.DATATYPE_DECIMAL:

			try {
				return (trimmedText == null ? null : new DecimalData(Double.parseDouble(trimmedText)));
			} catch (NumberFormatException nfe) {
				return null;
			}
			
		case Constants.DATATYPE_CHOICE:

			Vector selections = getSelections(text, q);
			return (selections.size() == 0 ? null : new SelectOneData((Selection)selections.elementAt(0)));
						
		case Constants.DATATYPE_CHOICE_LIST:

			return new SelectMultiData(getSelections(text, q));
			
		case Constants.DATATYPE_DATE_TIME:

			Date dt = (trimmedText == null ? null : DateUtils.parseDateTime(trimmedText));
			return (dt == null ? null : new DateTimeData(dt));

		case Constants.DATATYPE_DATE:

			Date d = (trimmedText == null ? null : DateUtils.parseDate(trimmedText));
			return (d == null ? null : new DateData(d));

		case Constants.DATATYPE_TIME:

			Date t = (trimmedText == null ? null : DateUtils.parseTime(trimmedText));
			return (t == null ? null : new TimeData(t));
			
		case Constants.DATATYPE_BOOLEAN:
			
			if(trimmedText == null) { 
				return null;
			} else {
				return trimmedText.equals("t") ? new BooleanData(true) : new BooleanData(false);
			}
		
		case Constants.DATATYPE_GEOPOINT:

            try {
                Vector gpv = (trimmedText == null ? null : DateUtils.split(trimmedText,",",false));
                double gp[] = new double[2];
                gp[0] = Double.parseDouble(((String)gpv.elementAt(0)));
                gp[1] = Double.parseDouble(((String)gpv.elementAt(1)));
                return new GeoPointData(gp);
            }   catch (NumberFormatException nfe) {
                return null;
            }
            
		default:

			return null;
		}
	}	

	private static Vector getSelections (String text, QuestionDef q) {
		Vector v = new Vector();
		
		Vector choices = DateUtils.split(text, XFormAnswerDataSerializer.DELIMITER, true);
		for (int i = 0; i < choices.size(); i++) {
			Selection s = getSelection((String)choices.elementAt(i), q);
			if (s != null)
				v.addElement(s);
		}
		
		return v;
	}
	
	private static Selection getSelection(String choice, QuestionDef q) {
		Selection s = new Selection(choice);
		
		if(q != null) {
			if (q.getSelectItems() == null)
				q.localizeSelectMap(null);
			int index = q.getSelectedItemIndex(choice); 
			s.attachQuestionDef(q);
			return (index != -1 ? s : null);
		}
		
		return s;
	}
}
