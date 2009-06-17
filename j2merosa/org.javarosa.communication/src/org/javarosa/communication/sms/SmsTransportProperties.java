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

package org.javarosa.communication.sms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.properties.IPropertyRules;

public class SmsTransportProperties implements IPropertyRules {
	public final static int MAX_SMS_SIZE = 140;
	public final static String POST_URL_LIST_PROPERTY = "PostURLlist";
	public final static String POST_URL_PROPERTY = "PostURL";
	
    Hashtable rules;
    Vector readOnlyProperties;
    
    public SmsTransportProperties() {
    	rules = new Hashtable();
    	readOnlyProperties = new Vector();
    	
        // PostURL List Property
        rules.put(POST_URL_LIST_PROPERTY, new Vector());
        readOnlyProperties.addElement(POST_URL_LIST_PROPERTY);
        Vector postList = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(POST_URL_LIST_PROPERTY);
        if(postList == null) {
        	JavaRosaServiceProvider.instance().getPropertyManager().setProperty(POST_URL_LIST_PROPERTY, new Vector());
        }
        

        // PostURL Property
        Vector postUrls = new Vector();
        postUrls.addElement(POST_URL_LIST_PROPERTY);
        rules.put(POST_URL_PROPERTY, postUrls);
    }

	
	public Vector allowableProperties() {
        Vector propList = new Vector();
        Enumeration iter = rules.keys();
        while (iter.hasMoreElements()) {
            propList.addElement(iter.nextElement());
        }
        return propList;
	}

	
	public Vector allowableValues(String propertyName) {
		return (Vector)rules.get(propertyName);
	}

	
	public boolean checkPropertyAllowed(String propertyName) {
        Enumeration iter = rules.keys();
        while (iter.hasMoreElements()) {
            if(propertyName.equals(iter.nextElement())) {
                return true;
            }
        }
        return false;
	}

	
	public boolean checkPropertyUserReadOnly(String propertyName) {
		return readOnlyProperties.contains(propertyName);
	}

	
	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		Vector prop = ((Vector)rules.get(propertyName));
        if(prop.size() != 0) {
            //Check whether this is a dynamic property
            if(prop.size() == 1 && checkPropertyAllowed((String)prop.elementAt(0))) {
                // If so, get its list of available values, and see whether the potentival value is acceptable.
                return ((Vector)JavaRosaServiceProvider.instance().getPropertyManager().getProperty((String)prop.elementAt(0))).contains(potentialValue);
            }
            else {
                return ((Vector)rules.get(propertyName)).contains(potentialValue);
            }
        }
        else
            return true;
	}

	
	public String getHumanReadableDescription(String propertyName) {
		return propertyName;
	}

	
	public String getHumanReadableValue(String propertyName, String value) {
		return value;
	}

	
	public void handlePropertyChanges(String propertyName) {
		// Nothing
	}
}
