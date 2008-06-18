package org.javarosa.properties;

import java.util.Enumeration;
import java.util.Vector;

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
public class PropertyManager {
    private static PropertyManager theInstance;
    public static final String PROPERTY_RMS = "PROPERTY_RMS_NEW";

    private IPropertyRules rules;
    private PropertyRMSUtility propertyRMS;

    /**
     * Singleton constructor for this PropertyManager
     */
    private PropertyManager() {
        this.propertyRMS = new PropertyRMSUtility(PROPERTY_RMS);
    }

    /**
     * Retrieve the Singleton instance of the property manager
     *
     * @return the Singleton instance of the property manager
     */
    public static PropertyManager instance() {
        if(theInstance == null) {
            theInstance = new PropertyManager();
        }
        return theInstance;
    }

    /**
     * Retrieves the singular property specified, as long as it exists in the current ruleset if one exists.
     *
     * @param propertyName the name of the property being retrieved
     * @return The String value of the property specified if it exists, is singluar, and is the current ruleset.
     * null if the property is denied by the current ruleset, or is a vector.
     */
    public String getSingularProperty(String propertyName) {
        if((rules == null || rules.checkPropertyAllowed(propertyName)) && propertyRMS.getValue(propertyName).size() == 1) {
            return (String)propertyRMS.getValue(propertyName).elementAt(0);
        }
        else {
            System.out.println("Warning: Singular property request failed");
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
        if(rules == null) {
            return propertyRMS.getValue(propertyName);
        }
        else {
            if(rules.checkPropertyAllowed(propertyName)) {
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
        if(rules == null) {
           propertyRMS.writeValue(propertyName, propertyValue);
        }
        else {
            boolean valid = true;
            Enumeration en = propertyValue.elements();
            while(en.hasMoreElements()) {
                if(!(rules.checkPropertyAllowed(propertyName) && rules.checkValueAllowed(propertyName, (String)en.nextElement()))) {
                    valid = false;
                }
            }
            if(valid) {
                propertyRMS.writeValue(propertyName, propertyValue);
            }
        }

    }

    /**
     * Retrieves the ruleset being used by this property manager if one exists.
     *
     * @return The ruleset being used by this property manager if one exists, null otherwise
     */
    public IPropertyRules getRules(){
        return rules;
    }

    /**
     * Sets the rules that should be used by this PropertyManager.
     *
     * @param rules The rules to be used. Null if no rules are desired.
     */
    public void setRules(IPropertyRules rules) {
        this.rules = rules;
    }
}