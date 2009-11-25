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

package org.javarosa.core.services.properties;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.locale.Localizer;

/**
 * A set of rules governing the allowable properties for JavaRosa's
 * core funtionality. 
 * 
 * @author ctsims
 *
 */
public class JavaRosaPropertyRules implements IPropertyRules {
    Hashtable rules;
    
    Vector readOnlyProperties;
    
    public final static String DEVICE_ID_PROPERTY = "DeviceID";
    public final static String CURRENT_LOCALE = "cur_locale";
    
    public final static String LOGS_ENABLED = "logenabled";
    
    public final static String LOGS_ENABLED_YES = "Enabled";
    public final static String LOGS_ENABLED_NO = "Disabled";

    /**
     * Creates the JavaRosa set of property rules
     */
    public JavaRosaPropertyRules() {
        rules = new Hashtable();
        readOnlyProperties = new Vector();

        //DeviceID Property
        rules.put(DEVICE_ID_PROPERTY, new Vector());
        Vector logs = new Vector();
        logs.addElement(LOGS_ENABLED_NO);
        logs.addElement(LOGS_ENABLED_YES);
        rules.put(LOGS_ENABLED, logs);
        
        rules.put(CURRENT_LOCALE, new Vector());
        
        readOnlyProperties.addElement(DEVICE_ID_PROPERTY);
        
    }

    /** (non-Javadoc)
     *  @see org.javarosa.properties.IPropertyRules#allowableValues(String)
     */
    public Vector allowableValues(String propertyName) {
    	if(CURRENT_LOCALE.equals(propertyName)) {
    		Localizer l = Localization.getGlobalLocalizerAdvanced();
    		Vector v = new Vector();
    		String[] locales = l.getAvailableLocales();
    		for(int i = 0 ; i < locales.length ; ++i) {
    			v.addElement(locales[i]);
    		}
    		return v;
    	}
        return (Vector)rules.get(propertyName);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.properties.IPropertyRules#checkValueAllowed(String, String)
     */
    public boolean checkValueAllowed(String propertyName, String potentialValue) {
    	if(CURRENT_LOCALE.equals(propertyName)) {
    		return Localization.getGlobalLocalizerAdvanced().hasLocale(potentialValue);
    	}
        Vector prop = ((Vector)rules.get(propertyName));
        if(prop.size() != 0) {
            //Check whether this is a dynamic property
            if(prop.size() == 1 && checkPropertyAllowed((String)prop.elementAt(0))) {
                // If so, get its list of available values, and see whether the potentival value is acceptable.
                return ((Vector)PropertyManager._().getProperty((String)prop.elementAt(0))).contains(potentialValue);
            }
            else {
                return ((Vector)rules.get(propertyName)).contains(potentialValue);
            }
        }
        else
            return true;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.properties.IPropertyRules#allowableProperties()
     */
    public Vector allowableProperties() {
        Vector propList = new Vector();
        Enumeration iter = rules.keys();
        while (iter.hasMoreElements()) {
            propList.addElement(iter.nextElement());
        }
        return propList;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.properties.IPropertyRules#checkPropertyAllowed)
     */
    public boolean checkPropertyAllowed(String propertyName) {
        Enumeration iter = rules.keys();
        while (iter.hasMoreElements()) {
            if(propertyName.equals(iter.nextElement())) {
                return true;
            }
        }
        return false;
    }
    
    /** (non-Javadoc)
     *  @see org.javarosa.properties.IPropertyRules#checkPropertyUserReadOnly)
     */
    public boolean checkPropertyUserReadOnly(String propertyName){
        return readOnlyProperties.contains(propertyName);
    }
    
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableDescription(java.lang.String)
     */
    public String getHumanReadableDescription(String propertyName) {
    	if(DEVICE_ID_PROPERTY.equals(propertyName)) {
    		return "Unique Device ID";
    	} else if(LOGS_ENABLED.equals(propertyName)) {
    		return "Device Logging";
    	} else if(CURRENT_LOCALE.equals(propertyName)) {
    		return Localization.get("settings.language");
    	}
    	return propertyName;
    }
    
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableValue(java.lang.String, java.lang.String)
     */
    public String getHumanReadableValue(String propertyName, String value) {
    	return value;
    }
    
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.properties.IPropertyRules#handlePropertyChanges(java.lang.String)
     */
    public void handlePropertyChanges(String propertyName) {
    	if(CURRENT_LOCALE.equals(propertyName)) {
    		String locale = PropertyManager._().getSingularProperty(propertyName);
    		Localization.setLocale(locale);
    	}
    }
}
