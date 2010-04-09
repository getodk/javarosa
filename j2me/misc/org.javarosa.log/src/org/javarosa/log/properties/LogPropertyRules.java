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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogPropertyRules implements IPropertyRules {
	Hashtable rules;
	Vector readOnlyProperties;

	public final static String LOG_SUBMIT_URL = "log_prop_submit";
	
	/**
	 * Creates the JavaRosa set of property rules
	 */
	public LogPropertyRules() {
		rules = new Hashtable();
		readOnlyProperties = new Vector();
		
		// Default properties
		rules.put(LOG_SUBMIT_URL, new Vector());
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
		Vector propList = new Vector();
		Enumeration iter = rules.keys();
		while (iter.hasMoreElements()) {
			propList.addElement(iter.nextElement());
		}
		return propList;
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
    		return "Log Report URL";
    	}
    	
    	return propertyName;
	}

	public String getHumanReadableValue(String propertyName, String value) {
		// What's this method for?
		return value;
	}

	public void handlePropertyChanges(String propertyName) {
		// nothing.  
		// what's this method for?
	}	

}
