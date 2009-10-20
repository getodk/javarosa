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

package org.javarosa.communication.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.properties.IPropertyRules;

/**
 * A set of rules for the properties of the Http Transport layer
 *
 * @author ctsims
 *
 */
public class HttpTransportProperties implements IPropertyRules {
    Hashtable rules;
    Vector readOnlyProperties;
    
    public final static String POST_URL_LIST_PROPERTY = "PostURLlist";
    public final static String POST_URL_PROPERTY = "PostURL";
    public final static String GET_URL_PROPERTY = "GetURL";
    public final static String AUTH_URL_PROPERTY = "AuthenticateURL";//for remote server user authentication

    /**
     * Creates the JavaRosa set of property rules
     */
    public HttpTransportProperties() {
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

        // GetURL Property
        Vector getUrls = new Vector();
        //getUrls.addElement(GET_URL_PROPERTY);
        rules.put(GET_URL_PROPERTY, getUrls);
        
        // AuthURL Property
        Vector authUrls = new Vector();
        //getUrls.addElement(GET_URL_PROPERTY);
        rules.put(AUTH_URL_PROPERTY, authUrls);
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
                return ((Vector)JavaRosaServiceProvider.instance().getPropertyManager().getProperty((String)prop.elementAt(0))).contains(potentialValue);
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
    	if(POST_URL_LIST_PROPERTY.equals(propertyName)) {
    		return "List of possible POST URL's";
    	} else if(POST_URL_PROPERTY.equals(propertyName)) {
    		return "Current URL for POST's";
    	} else if(GET_URL_PROPERTY.equals(propertyName)) {
    		return "Current URL for GET's";
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
    	//Nothing
    }
}
