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

package org.javarosa.formmanager.properties;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;

/**
 * Properties for form management and entry.
 * 
 * @author Clayton Sims
 *
 */
public class FormManagerProperties implements IPropertyRules {
	    Hashtable rules;
	    Vector readOnlyProperties;

	    public final static String VIEW_TYPE_PROPERTY = "ViewStyle";
		
		// View Types
	    public static final String VIEW_CHATTERBOX = "v_chatterbox";
	    public static final String VIEW_SINGLEQUESTIONSCREEN = "v_singlequestionscreen";
	    
	    /**
	     * Creates the JavaRosa set of property rules
	     */
	    public FormManagerProperties() {
	        rules = new Hashtable();
	        readOnlyProperties = new Vector();
	        	        

	        Vector allowableDisplays = new Vector();
	        allowableDisplays.addElement(VIEW_CHATTERBOX);
	        allowableDisplays.addElement(VIEW_SINGLEQUESTIONSCREEN);
	        rules.put(VIEW_TYPE_PROPERTY, allowableDisplays);

	    }

	    /** (non-Javadoc)
	     *  @see org.javarosa.properties.IPropertyRules#allowableValues(String)
	     */
	    public Vector allowableValues(String propertyName) {
	        return (Vector)rules.get(propertyName);
	    }

	    /** (non-Javadoc)
	     *  @see org.javarosa.properties.IPropertyRules#checkValueAllowed(String, String)
	     */
	    public boolean checkValueAllowed(String propertyName, String potentialValue) {
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
	    	if(VIEW_TYPE_PROPERTY.equals(propertyName)) {
	    		return "Form Entry View";
	    	}
	    	return propertyName;
	    }
	    
	    /*
	     * (non-Javadoc)
	     * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableValue(java.lang.String, java.lang.String)
	     */
	    public String getHumanReadableValue(String propertyName, String value) {
	    	if(VIEW_TYPE_PROPERTY.equals(propertyName)) {
	    		if(VIEW_CHATTERBOX.equals(value)) {
	    			return "Chatterbox";
	    		} else if(VIEW_SINGLEQUESTIONSCREEN.equals(value)) {
	    			return "One Question Per Screen"; 
	    		}
	     	}
	    	return value;
	    }
	    /*
	     * (non-Javadoc)
	     * @see org.javarosa.core.services.properties.IPropertyRules#handlePropertyChanges(java.lang.String)
	     */
	    public void handlePropertyChanges(String propertyName) {
	    	//Is there anything we can do here?
	    }
}
