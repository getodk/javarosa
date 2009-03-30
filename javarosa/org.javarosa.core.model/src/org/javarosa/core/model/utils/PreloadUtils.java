/**
 * 
 */
package org.javarosa.core.model.utils;

import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.StringData;

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
		
		if(o instanceof String) {
			return new StringData((String)o);
		} else if(o instanceof Date) {
			return new DateData((Date)o);
		} else if(o instanceof Vector) {
			return new SelectMultiData((Vector)o);
		} else if(o instanceof IAnswerData) {
			return (IAnswerData)o;
		}
		return new StringData(o.toString());
	}
}
