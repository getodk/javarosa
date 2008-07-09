package org.javarosa.core.services;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.services.properties.storage.PropertyRMSUtility;

/**
 * PropertyManager is a singleton class that is used to set and retrieve name/value pairs
 * from persistent storage.
 *
 * Which properties are allowed, and what they can be set to, can be specified by an implementation of
 * the IPropertyRules interface.
 *
 * @author ctsims
 *
 */
public class PropertyManager implements IService {
    public static final String PROPERTY_RMS = "PROPERTY_RMS_NEW";

    private Vector rulesList;
    private PropertyRMSUtility propertyRMS;
    
    public String getName() {
    	return "Property Manager";
    }

    /**
     * Constructor for this PropertyManager
     */
    public PropertyManager() {
        this.propertyRMS = new PropertyRMSUtility(PROPERTY_RMS);
        rulesList = new Vector();
    }

    /**
     * Retrieves the singular property specified, as long as it exists in the current ruleset if one exists.
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, is singluar, and is the current ruleset.
     * null if the property is denied by the current ruleset, or is a vector.
     */
    public String getSingularProperty(String propertyName) {
        if((rulesList.size() == 0 || checkPropertyAllowed(propertyName)) && propertyRMS.getValue(propertyName).size() == 1) {
            return (String)propertyRMS.getValue(propertyName).elementAt(0);
        }
        else {
            System.out.println("Warning: Singular property request failed for property " + propertyName);
            return null;
        }
    }

    
    /**
     * Retrieves the property specified, as long as it exists in the current ruleset if one exists.
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, and is the current ruleset, if one exists.
     * null if the property is denied by the current ruleset.
     */
    public Vector getProperty(String propertyName) {
        if(rulesList.size() == 0) {
            return propertyRMS.getValue(propertyName);
        }
        else {
            if(checkPropertyAllowed(propertyName)) {
                return propertyRMS.getValue(propertyName);
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
        if(rulesList.size() == 0) {
           propertyRMS.writeValue(propertyName, propertyValue);
        }
        else {
            boolean valid = true;
            Enumeration en = propertyValue.elements();
            while(en.hasMoreElements()) {
                if(!(checkPropertyAllowed(propertyName) && checkValueAllowed(propertyName, (String)en.nextElement()))) {
                    valid = false;
                }
            }
            if(valid) {
                propertyRMS.writeValue(propertyName, propertyValue);
            }
        }

    }

    /**
     * Retrieves the set of rules being used by this property manager if any exist.
     *
     * @return The ruleset being used by this property manager if one exists, null otherwise
     */
    public Vector getRules(){
        return rulesList;
    }

    /**
     * Sets the rules that should be used by this PropertyManager.
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
    
    public boolean checkPropertyAllowed(String propertyName) {
    	if(rulesList.size() == 0) {
    		return true;
    	} else {
    		boolean allowed = false;
    		Enumeration en = rulesList.elements();
    		while(en.hasMoreElements()) {
    			IPropertyRules rules = (IPropertyRules)en.nextElement();
    			if(rules.checkPropertyAllowed(propertyName)) {
    				allowed = true;
    			}
    		}
    		return allowed;
    	}
    }
    
    public boolean checkValueAllowed(String propertyName,
			String propertyValue) {
		if (rulesList.size() == 0) {
			return true;
		} else {
			boolean allowed = false;
			Enumeration en = rulesList.elements();
			while (en.hasMoreElements()) {
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
    
}