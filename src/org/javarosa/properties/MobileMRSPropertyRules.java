package org.javarosa.properties;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.util.SimpleOrderedHashtable;

/**
 * A set of rules for the properties in the MobileMRS application
 * 
 * @author ctsims
 *
 */
public class MobileMRSPropertyRules implements IPropertyRules {
    SimpleOrderedHashtable rules;
    public final static String VIEW_TYPE_PROPERTY = "ViewStyle";
    
    public MobileMRSPropertyRules() {
        rules = new SimpleOrderedHashtable();
        
        Vector allowableKeys = new Vector();
        allowableKeys.addElement(VIEW_TYPE_PROPERTY);
        
        Vector allowableDisplays = new Vector();
        allowableDisplays.addElement(Constants.VIEW_CHATTERBOX);
        allowableDisplays.addElement(Constants.VIEW_CLFORMS);
        rules.put(VIEW_TYPE_PROPERTY, allowableDisplays);
        
    }
    
    public Vector allowableValues(String propertyName) {
        return (Vector)rules.get(propertyName);
    }
    
    public boolean checkValueAllowed(String propertyName, String potentialValue) {
        return ((Vector)rules.get(propertyName)).contains(potentialValue);
    }
    
    public Vector allowableProperties() {
        Vector properties = new Vector();
        Enumeration iter = rules.keys();
        while (iter.hasMoreElements()) {
            properties.addElement(iter.nextElement());
        }
        return properties;
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
}
