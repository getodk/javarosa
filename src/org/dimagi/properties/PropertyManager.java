package org.dimagi.properties;

public class PropertyManager {
    private static PropertyManager theInstance;
    public static final String PROPERTY_RMS = "PROPERTY_RMS_NEW";
    private PropertyRMSUtility propertyRMS;
    
    private PropertyManager() {
        this.propertyRMS = new PropertyRMSUtility(PROPERTY_RMS);
    }
    
    public static PropertyManager instance() {
        if(theInstance == null) {
            theInstance = new PropertyManager();
        }
        return theInstance;
    }
    
    public String getProperty(String propertyName) {
        return propertyRMS.getValue(propertyName);
    }
    
    public void setProperty(String propertyName, String propertyValue) {
        propertyRMS.writeValue(propertyName, propertyValue);
    }

}
