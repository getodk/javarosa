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
public class JavaRosaPropertyRules implements IPropertyRules {
    SimpleOrderedHashtable rules;
    public final static String VIEW_TYPE_PROPERTY = "ViewStyle";
    
    /**
     * Creates the JavaRosa set of property rules
     */
    public JavaRosaPropertyRules() {
        rules = new SimpleOrderedHashtable();
        
        Vector allowableKeys = new Vector();
        allowableKeys.addElement(VIEW_TYPE_PROPERTY);
        
        Vector allowableDisplays = new Vector();
        allowableDisplays.addElement(Constants.VIEW_CHATTERBOX);
        allowableDisplays.addElement(Constants.VIEW_CLFORMS);
        allowableDisplays.addElement(Constants.VIEW_CUSTOMCHAT);
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
        return ((Vector)rules.get(propertyName)).contains(potentialValue);
    }
    
    /** (non-Javadoc)
     *  @see org.javarosa.properties.IPropertyRules#allowableProperties()
     */
    public Vector allowableProperties() {
        Vector properties = new Vector();
        Enumeration iter = rules.keys();
        while (iter.hasMoreElements()) {
            properties.addElement(iter.nextElement());
        }
        return properties;
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
}
