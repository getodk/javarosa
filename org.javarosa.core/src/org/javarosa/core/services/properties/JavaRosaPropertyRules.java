package org.javarosa.core.services.properties;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaPlatform;

/**
 * A set of rules for the properties for default JavaRosa applications
 *
 * @author ctsims
 *
 */
public class JavaRosaPropertyRules implements IPropertyRules {
    Hashtable rules;
    Vector readOnlyProperties;
    
    public final static String DEVICE_ID_PROPERTY = "DeviceID";

    /**
     * Creates the JavaRosa set of property rules
     */
    public JavaRosaPropertyRules() {
        rules = new Hashtable();
        readOnlyProperties = new Vector();

        //DeviceID Property
        rules.put(DEVICE_ID_PROPERTY, new Vector());
        readOnlyProperties.addElement(DEVICE_ID_PROPERTY);
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
        System.out.println("Checking rules for: " + propertyName);
        Vector prop = ((Vector)rules.get(propertyName));
        System.out.println("They are: " + prop.toString());
        if(prop.size() != 0) {
            //Check whether this is a dynamic property
            if(prop.size() == 1 && checkPropertyAllowed((String)prop.elementAt(0))) {
                // If so, get its list of available values, and see whether the potentival value is acceptable.
                return ((Vector)JavaRosaPlatform.instance().getPropertyManager().getProperty((String)prop.elementAt(0))).contains(potentialValue);
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
}
