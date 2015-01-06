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

/**
 * 
 */
package org.javarosa.core.model.utils;

import java.util.Date;
import java.util.List;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;

/**
 * @author Clayton Sims
 * @date Mar 30, 2009 
 *
 */
public class PreloadUtils {
	
	/**
	 * Note: This method is a hack to fix the problem that we don't know what
	 * data type we're using when we have a preloader. That should get fixed, 
	 * and this method should be removed.
	 * @param o
	 * @return
	 */
	public static IAnswerData wrapIndeterminedObject(Object o) {
		if(o == null) {
			return null;
		}
		
		//TODO: Replace this all with an uncast data
		if(o instanceof String) {
			return new StringData((String)o);
		} else if(o instanceof Date) {
			return new DateData((Date)o);
		} else if (o instanceof Integer) {
			return new IntegerData((Integer)o);
		} else if (o instanceof Long) {
			return new LongData((Long)o);
		} else if (o instanceof Double) {
			return new DecimalData((Double)o);
		} else if(o instanceof List) {
			return new SelectMultiData((List<Selection>)o);
		} else if(o instanceof IAnswerData) {
			return (IAnswerData)o;
		}
		return new StringData(o.toString());
	}
}
