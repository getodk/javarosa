package org.javarosa.communication.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaPlatform;
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
    
    public final static String DEVICE_ID_PROPERTY = "DeviceID";
    public final static String POST_URL_LIST_PROPERTY = "PostURLlist";
    public final static String POST_URL_PROPERTY = "PostURL";
    public final static String GET_URL_PROPERTY = "GetURL";

    /**
     * Creates the JavaRosa set of property rules
     */
    public HttpTransportProperties() {
        rules = new Hashtable();
        readOnlyProperties = new Vector();


        // PostURL List Property
        rules.put(POST_URL_LIST_PROPERTY, new Vector());
        readOnlyProperties.addElement(POST_URL_LIST_PROPERTY);

        // PostURL Property
        Vector postUrls = new Vector();
        postUrls.addElement(POST_URL_PROPERTY);
        rules.put(POST_URL_PROPERTY, postUrls);

        // GetURL Property
        Vector getUrls = new Vector();
        getUrls.addElement(GET_URL_PROPERTY);
        rules.put(GET_URL_PROPERTY, getUrls);
        
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
