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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
    HashMap<String,ArrayList<String>> rules;

    ArrayList<String> readOnlyProperties;

    public final static String DEVICE_ID_PROPERTY = "DeviceID";
    public final static String CURRENT_LOCALE = "cur_locale";

    public final static String LOGS_ENABLED = "logenabled";

    public final static String LOGS_ENABLED_YES = "Enabled";
    public final static String LOGS_ENABLED_NO = "Disabled";

    /** The expected compliance version for the OpenRosa API set **/
    public final static String OPENROSA_API_LEVEL = "jr_openrosa_api";

    /**
     * Creates the JavaRosa set of property rules
     */
    public JavaRosaPropertyRules() {
        rules = new HashMap<String,ArrayList<String>>();
        readOnlyProperties = new ArrayList<String>(2);

        //DeviceID Property
        rules.put(DEVICE_ID_PROPERTY, new ArrayList<String>(1));
        ArrayList<String> logs = new ArrayList<String>(2);
        logs.add(LOGS_ENABLED_NO);
        logs.add(LOGS_ENABLED_YES);
        rules.put(LOGS_ENABLED, logs);

        rules.put(CURRENT_LOCALE, new ArrayList<String>(1));

        rules.put(OPENROSA_API_LEVEL, new ArrayList<String>(1));

        readOnlyProperties.add(DEVICE_ID_PROPERTY);
        readOnlyProperties.add(OPENROSA_API_LEVEL);

    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#allowableValues(String)
     */
    public ArrayList<String> allowableValues(String propertyName) {
    	if(CURRENT_LOCALE.equals(propertyName)) {
    		Localizer l = Localization.getGlobalLocalizerAdvanced();
    		String[] locales = l.getAvailableLocales();
    		ArrayList<String> v = new ArrayList<String>(locales.length);
    		for ( String locale : locales ) {
    		   v.add(locale);
    		}
    		return v;
    	}
      return rules.get(propertyName);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#checkValueAllowed(String, String)
     */
    public boolean checkValueAllowed(String propertyName, String potentialValue) {
    	if(CURRENT_LOCALE.equals(propertyName)) {
    		return Localization.getGlobalLocalizerAdvanced().hasLocale(potentialValue);
    	}
    	ArrayList<String> prop = rules.get(propertyName);
        if(prop.size() != 0) {
            //Check whether this is a dynamic property
            if(prop.size() == 1 && checkPropertyAllowed(prop.get(0))) {
                // If so, get its list of available values, and see whether the potentival value is acceptable.
                return PropertyManager._().getProperty(prop.get(0)).contains(potentialValue);
            }
            else {
                return rules.get(propertyName).contains(potentialValue);
            }
        }
        else
            return true;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#allowableProperties()
     */
    public ArrayList<String> allowableProperties() {
    	Set<String> keys = rules.keySet();
    	ArrayList<String> propList = new ArrayList<String>(keys);
      return propList;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyAllowed)
     */
    public boolean checkPropertyAllowed(String propertyName) {
       return rules.containsKey(propertyName);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyUserReadOnly)
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
    	} else if(OPENROSA_API_LEVEL.equals(propertyName)) {
    		return "OpenRosa API Level";
    	}
    	return propertyName;
    }

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableValue(java.lang.String, java.lang.String)
     */
    public String getHumanReadableValue(String propertyName, String value) {
    	if(CURRENT_LOCALE.equals(propertyName)) {
    		String name = Localization.getGlobalLocalizerAdvanced().getText(value);
    		if(name != null) {
    			return name;
    		}
    	}
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
