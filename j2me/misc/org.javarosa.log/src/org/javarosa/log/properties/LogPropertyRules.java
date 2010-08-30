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
package org.javarosa.log.properties;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogPropertyRules implements IPropertyRules {
	Hashtable rules;
	Vector readOnlyProperties;

	public final static String LOG_SUBMIT_URL = "log_prop_submit";
	
	public final static String LOG_WEEKLY_SUBMIT = "log_prop_weekly";
	
	public final static String LOG_DAILY_SUBMIT = "log_prop_daily";
	
	public final static String NEVER = "log_never";
	public final static String SHORT = "log_short";
	public final static String FULL = "log_full";
	
	public final static String LOG_NEXT_DAILY_SUBMIT = "log_prop_next_daily";
	public final static String LOG_NEXT_WEEKLY_SUBMIT = "log_prop_next_weekly";
	
	/**
	 * Creates the JavaRosa set of property rules
	 */
	public LogPropertyRules() {
		rules = new Hashtable();
		readOnlyProperties = new Vector();
		
		// Default properties
		rules.put(LOG_SUBMIT_URL, new Vector());
		
		Vector submissionTypes = new Vector();
		submissionTypes.addElement(NEVER);
		submissionTypes.addElement(SHORT);
		submissionTypes.addElement(FULL);
		
		rules.put(LOG_WEEKLY_SUBMIT, submissionTypes);
		rules.put(LOG_DAILY_SUBMIT, submissionTypes);
		
		rules.put(LOG_NEXT_DAILY_SUBMIT, new Vector());
		readOnlyProperties.addElement(LOG_NEXT_DAILY_SUBMIT);
		rules.put(LOG_NEXT_WEEKLY_SUBMIT, new Vector());
		readOnlyProperties.addElement(LOG_NEXT_WEEKLY_SUBMIT);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.properties.IPropertyRules#allowableValues(String)
	 */
	public Vector allowableValues(String propertyName) {
		return (Vector) rules.get(propertyName);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.properties.IPropertyRules#checkValueAllowed(String,
	 *      String)
	 */
	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		Vector prop = ((Vector) rules.get(propertyName));
		if (prop.size() != 0) {
			// Check whether this is a dynamic property
			if (prop.size() == 1
					&& checkPropertyAllowed((String) prop.elementAt(0))) {
				// If so, get its list of available values, and see whether the
				// potential value is acceptable.
				return ((Vector) PropertyManager._().getProperty(
								(String) prop.elementAt(0)))
						.contains(potentialValue);
			} else {
				return ((Vector) rules.get(propertyName))
						.contains(potentialValue);
			}
		} else
			return true;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.properties.IPropertyRules#allowableProperties()
	 */
	public Vector allowableProperties() {
		//TEST: If logs are disabled, none of these properties are meaningful 
		//and they should be disabled to not confuse users.
		if(JavaRosaPropertyRules.LOGS_ENABLED_YES.equals(PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED))) {
			Vector propList = new Vector();
			Enumeration iter = rules.keys();
			while (iter.hasMoreElements()) {
				propList.addElement(iter.nextElement());
			}
			return propList;
		} else {
			return new Vector();
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.properties.IPropertyRules#checkPropertyAllowed)
	 */
	public boolean checkPropertyAllowed(String propertyName) {
		Enumeration iter = rules.keys();
		while (iter.hasMoreElements()) {
			if (propertyName.equals(iter.nextElement())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.properties.IPropertyRules#checkPropertyUserReadOnly)
	 */
	public boolean checkPropertyUserReadOnly(String propertyName) {
			return readOnlyProperties.contains(propertyName);
	}

	public String getHumanReadableDescription(String propertyName) {
		if(LOG_SUBMIT_URL.equals(propertyName)) {
    		return Localization.get("log.submit.url");
    	} else if(LOG_WEEKLY_SUBMIT.equals(propertyName)) {
    		return Localization.get("log.submit.weekly");
    	} else if(LOG_DAILY_SUBMIT.equals(propertyName)) {
    		return Localization.get("log.submit.nigthly");
    	} else if(LOG_NEXT_DAILY_SUBMIT.equals(propertyName)) {
    		return Localization.get("log.submit.next.daily");
    	} else if(LOG_NEXT_WEEKLY_SUBMIT.equals(propertyName)) {
        	return Localization.get("log.submit.next.weekly");
        }
    	return propertyName;
	}

	public String getHumanReadableValue(String propertyName, String value) {
		if(LOG_WEEKLY_SUBMIT.equals(propertyName) || LOG_DAILY_SUBMIT.equals(propertyName)) {
			if(NEVER.equals(value)) {
				return Localization.get("log.submit.never");
			} else if(SHORT.equals(value)) {
				return Localization.get("log.submit.short");
			} else if(FULL.equals(value)) {
				return Localization.get("log.submit.full");
			}
		}
		if(LOG_NEXT_DAILY_SUBMIT.equals(propertyName) || LOG_NEXT_WEEKLY_SUBMIT.equals(propertyName)) {
			try {
				if(value == null) {
					return "ASAP";
				} else {
					long next = Long.parseLong(value);
					return DateUtils.formatDate(new Date(next), DateUtils.FORMAT_ISO8601);
				}
			} catch(Exception e) {
				return "error";
			}
		}
		return value;
	}

	public void handlePropertyChanges(String propertyName) {
		//Maybe init the daily/weekly health reports here if
		//the value of logs enabled changes?
	}	

}
