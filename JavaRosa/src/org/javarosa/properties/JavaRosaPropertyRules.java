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
    Vector readOnlyProperties;
    public final static String VIEW_TYPE_PROPERTY = "ViewStyle";
    public final static String GET_FORMS_METHOD = "GetFormsMethod";
    public final static String POST_URL_PROPERTY = "PostURL";
    public final static String GET_URL_PROPERTY = "GetURL";
    public final static String DEVICE_ID_PROPERTY = "DeviceID";

    public final static String HEALTH_UNIT_PROPERTY = "health-unit";
    public final static String HEALTH_UNIT_CODE_PROPERTY = "health-unit-code";
    public final static String SUBCOUNTY_PROPERTY = "sub-county";
    public final static String DISTRICT_PROPERTY = "district";
    public final static String HSD_PROPERTY = "hsd";    
    public final static String FIN_YEAR_PROPERTY = "fin-year";    
    
    /**
     * Creates the JavaRosa set of property rules
     */
    public JavaRosaPropertyRules() {
        rules = new SimpleOrderedHashtable();
        readOnlyProperties = new Vector();

        // View Property
        Vector allowableDisplays = new Vector();
        allowableDisplays.addElement(Constants.VIEW_CHATTERBOX);
        allowableDisplays.addElement(Constants.VIEW_CLFORMS);
        allowableDisplays.addElement(Constants.VIEW_CUSTOMCHAT);
        rules.put(VIEW_TYPE_PROPERTY, allowableDisplays);

        // GetFormsMethod Property
        Vector getMethods = new Vector();
        getMethods.addElement(Constants.GETFORMS_AUTOHTTP);
        getMethods.addElement(Constants.GETFORMS_EVGETME);
        getMethods.addElement(Constants.GETFORMS_BLUETOOTH);
        getMethods.addElement(Constants.GETFORMS_FILE);
        rules.put(GET_FORMS_METHOD, getMethods);
        
        
        // PostURL List Property
        rules.put(Constants.POST_URL_LIST, new Vector());
        readOnlyProperties.addElement(Constants.POST_URL_LIST);

        // PostURL Property
        Vector postUrls = new Vector();
        postUrls.addElement(Constants.POST_URL_LIST);
        rules.put(POST_URL_PROPERTY, postUrls);

        // GetURL Property
        Vector getUrls = new Vector();
        getUrls.addElement(Constants.GET_URL);
        rules.put(GET_URL_PROPERTY, getUrls);
        
        //DeviceID Property
        rules.put(DEVICE_ID_PROPERTY, new Vector());
        readOnlyProperties.addElement(DEVICE_ID_PROPERTY);

        //Site-specific Fields
        rules.put(HEALTH_UNIT_PROPERTY, new Vector());
        rules.put(HEALTH_UNIT_CODE_PROPERTY, new Vector());
        rules.put(SUBCOUNTY_PROPERTY, new Vector());
        rules.put(DISTRICT_PROPERTY, new Vector());
        rules.put(HSD_PROPERTY, new Vector());
        rules.put(FIN_YEAR_PROPERTY, new Vector());
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
                return ((Vector)PropertyManager.instance().getProperty((String)prop.elementAt(0))).contains(potentialValue);
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
