package org.javarosa.demo.properties;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaPlatform;

/**
 * Application-specific properties for the JavaRosa reference
 * implementation.
 * 
 * @author Clayton Sims
 *
 */
public class DemoAppProperties {
	    Hashtable rules;
	    Vector readOnlyProperties;

	    public final static String HEALTH_UNIT_PROPERTY = "health-unit";
	    public final static String HEALTH_UNIT_CODE_PROPERTY = "health-unit-code";
	    public final static String SUBCOUNTY_PROPERTY = "sub-county";
	    public final static String DISTRICT_PROPERTY = "district";
	    public final static String HSD_PROPERTY = "hsd";    
	    public final static String FIN_YEAR_PROPERTY = "fin-year";    
	    
	    /**
	     * Creates the JavaRosa set of property rules
	     */
	    public DemoAppProperties() {
	        rules = new Hashtable();
	        readOnlyProperties = new Vector();
	        	        

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
