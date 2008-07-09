package org.javarosa.core.services.properties;

import java.util.Vector;

/**
 * The IPropertyRules interface is used to describe a set of rules for what properties are allowed for a given
 * property manager, and what values are are acceptable for a given property.
 *  
 * @author ctsims
 *
 */
public interface IPropertyRules {
    /**
     * Identifies what values are acceptable for a given property
     * 
     * @param propertyName The name of the property that is being identified
     * @return A Vector containing all of the values that this property may be set to
     */
    public Vector allowableValues(String propertyName);
    
    /**
     * Identifies whether the given value is an acceptable for a property.
     * 
     * @param propertyName The name of the property that is being identified
     * @param potentialValue The value that is being tested 
     * 
     * @return True if the property specified may be set to potentialValue, False otherwise
     */
    public boolean checkValueAllowed(String propertyName, String potentialValue);
    
    /**
     * Identifies what properties are acceptable for this rules set
     * 
     * @return A Vector containing all of the properties that may be set
     */
    public Vector allowableProperties();
    
    /**
     * Identifies whether the given property is usable
     * 
     * @param propertyName The name of the property that is being tested
     * 
     * @return True if the property specified may used. False otherwise
     */
    public boolean checkPropertyAllowed(String propertyName);
    
    /**
     * Identifies whether the given property is read-only for end-users
     * 
     * @param propertyName The name of the property that is being tested
     * 
     * @return True if the property specified may not be modified by the user. False otherwise
     */
    public boolean checkPropertyUserReadOnly(String propertyName);
}
