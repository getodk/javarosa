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

package org.javarosa.core.services;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.services.properties.Property;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;

/**
 * PropertyManager is a class that is used to set and retrieve name/value pairs
 * from persistent storage.
 *
 * Which properties are allowed, and what they can be set to, can be specified by an implementation of
 * the IPropertyRules interface, any number of which can be registered with a property manager. All 
 * property rules are inclusive, and can only increase the number of potential properties or property
 * values.
 *
 * @author Clayton Sims
 *
 */
public class PropertyManager implements IService,IPropertyManager {
	/**
	 * The name for the Persistent storage utility name
	 */
    public static final String STORAGE_KEY = "PROPERTY";

    /**
     * The list of rules 
     */
    private Vector rulesList;
    
    /**
     * The persistent storage utility
     */
    private IStorageUtilityIndexed properties;
    
    public String getName() {
    	return "Property Manager";
    }

    /**
     * Constructor for this PropertyManager
     */
    public PropertyManager() {
    	this.properties = (IStorageUtilityIndexed)StorageManager.getStorage(STORAGE_KEY);
    	rulesList = new Vector();
    }

    /**
     * Retrieves the singular property specified, as long as it exists in one of the current rulesets
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, is singluar, and is in one the current
     * rulessets. null if the property is denied by the current ruleset, or is a vector.
     */
    public String getSingularProperty(String propertyName) {
    	String retVal = null;
        if((rulesList.size() == 0 || checkPropertyAllowed(propertyName))) {
        	Vector value = getValue(propertyName);
        	if(value != null && value.size() == 1) {
        		retVal = (String)value.elementAt(0);
        	}
        }
        if(retVal == null) {
    		//#if debug.output==verbose
            System.out.println("Warning: Singular property request failed for property " + propertyName);
            //#endif
        }
        return retVal;
    }

    
    /**
     * Retrieves the property specified, as long as it exists in one of the current rulesets
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, and is the current ruleset, if one exists.
     * null if the property is denied by the current ruleset.
     */
    public Vector getProperty(String propertyName) {
        if(rulesList.size() == 0) {
            return getValue(propertyName);
        }
        else {
            if(checkPropertyAllowed(propertyName)) {
                return getValue(propertyName);
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Sets the given property to the given string value, if both are allowed by any existing ruleset
     * @param propertyName The property to be set
     * @param propertyValue The value that the property will be set to
     */
    public void setProperty(String propertyName, String propertyValue) {
        Vector wrapper = new Vector();
        wrapper.addElement(propertyValue);
        setProperty(propertyName, wrapper);
    }
    
    /**
     * Sets the given property to the given vector value, if both are allowed by any existing ruleset
     * @param propertyName The property to be set
     * @param propertyValue The value that the property will be set to
     */
    public void setProperty(String propertyName, Vector propertyValue) {
    	Vector oldValue = getProperty(propertyName);
    	if(oldValue != null && vectorEquals(oldValue, propertyValue)) {
    		//No point in redundantly setting values!
    		return;
    	}
        if(rulesList.size() == 0) {
           writeValue(propertyName, propertyValue);
        }
        else {
            boolean valid = true;
            Enumeration en = propertyValue.elements();
            while(en.hasMoreElements()) {
                // RL - checkPropertyAllowed is implicit in checkValueAllowed
                if (!checkValueAllowed(propertyName, (String)en.nextElement())) {
                    valid = false;
                } 
            }
            if(valid) {
                writeValue(propertyName, propertyValue);
                notifyChanges(propertyName);
            }
            //#if debug.output==verbose
            else {
            	System.out.println("Property Manager: Unable to write value (" + propertyValue + ") to " + propertyName);
            }
            //#endif
        }

    }
    
    private boolean vectorEquals(Vector v1, Vector v2) {
    	if(v1.size() != v2.size()) {
    		return false;
    	} else {
    		for(int i = 0; i < v1.size() ; ++i ) {
    			if(!v1.elementAt(i).equals(v2.elementAt(i))) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

    /**
     * Retrieves the set of rules being used by this property manager if any exist.
     *
     * @return The rulesets being used by this property manager
     */
    public Vector getRules(){
        return rulesList;
    }

    /**
     * Sets the rules that should be used by this PropertyManager, removing any other
     * existing rules sets.
     *
     * @param rules The rules to be used. 
     */
    public void setRules(IPropertyRules rules) {
        this.rulesList.removeAllElements();
        this.rulesList.addElement(rules);
    }
    
    /**
     * Adds a set of rules to be used by this PropertyManager.
     * Note that rules sets are inclusive, they add new possible
     * values, never remove possible values.
     * 
     * @param rules The set of rules to be added to the permitted list
     */
    public void addRules(IPropertyRules rules) {
    	if(rules != null) {
    		this.rulesList.addElement(rules);
    	}
    }
    
    /**
     * Checks that a property is permitted to exist by any of the existing rules sets
     * 
     * @param propertyName The name of the property to be set
     * @return true if the property is permitted to store values. false otherwise
     */
    public boolean checkPropertyAllowed(String propertyName) {
    	if(rulesList.size() == 0) {
    		return true;
    	} else {
    		boolean allowed = false;
    		Enumeration en = rulesList.elements();
    		//We're fine if we return true, inclusive rules sets
    		while(en.hasMoreElements() && !allowed) {
    			IPropertyRules rules = (IPropertyRules)en.nextElement();
    			if(rules.checkPropertyAllowed(propertyName)) {
    				allowed = true;
    			}
    		}
    		return allowed;
    	}
    }
    
    /**
     * Checks that a property is allowed to store a certain value.
     * 
     * @param propertyName The name of the property to be set
     * @param propertyValue The value to be stored in the given property
     * @return true if the property given is allowed to be stored. false otherwise.
     */
    public boolean checkValueAllowed(String propertyName,
			String propertyValue) {
		if (rulesList.size() == 0) {
			return true;
		} else {
			boolean allowed = false;
			Enumeration en = rulesList.elements();
			while (en.hasMoreElements() && !allowed) {
				IPropertyRules rules = (IPropertyRules) en.nextElement();
				if (rules.checkPropertyAllowed(propertyName)) {
					if (rules.checkValueAllowed(propertyName, propertyValue)) {
						allowed = true;
					}
				}
			}
			return allowed;
		}
	}
    
    /**
     * Identifies the property rules set that the property belongs to, and notifies
     * it about the property change.
     * 
     * @param property The property that has been changed 
     */
    private void notifyChanges(String property) {
    	if(rulesList.size() ==0 ) { 
    		return;
    	} 
    	
    	boolean notified = false;
    	Enumeration rules = rulesList.elements();
    	while(rules.hasMoreElements() && !notified) {
    		IPropertyRules therules = (IPropertyRules)rules.nextElement();
    		if(therules.checkPropertyAllowed(property)) {
    			therules.handlePropertyChanges(property);
    		}
    	}
    	
    }

    public Vector getValue (String name) {
    	Property p = (Property)properties.getRecordForValue("NAME", name);
    	return (p != null ? p.value : null);
    }
    
    public void writeValue(String propertyName, Vector value) {
        Property theProp = new Property();
        theProp.name = propertyName;
        theProp.value = value;

        Vector IDs = properties.getIDsForValue("NAME", propertyName);
        if (IDs.size() == 1) {
        	theProp.setID(((Integer)IDs.elementAt(0)).intValue());
        }
        
        try {
        	properties.write(theProp);
        } catch (StorageFullException e) {
			throw new RuntimeException("uh-oh, storage full [properties]"); //TODO: handle this
        }
    }
    
}
