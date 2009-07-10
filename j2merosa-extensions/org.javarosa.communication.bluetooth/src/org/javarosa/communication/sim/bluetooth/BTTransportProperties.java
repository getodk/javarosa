package org.javarosa.communication.sim.bluetooth;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.properties.IPropertyRules;
/**
 * A set of rules for the properties of the Bluetooth Transport layer
 *
 * @author smuwanga(Simon Peter Muwanga)
 *
 */
public class BTTransportProperties implements IPropertyRules{
	Hashtable rules;
    Vector readOnlyProperties;
    
    public final static String POST_URL_LIST_PROPERTY = "PostURLlist";
    public final static String POST_URL_PROPERTY = "PostURL";
    public final static String GET_URL_PROPERTY = "GetURL";
    public final static String AUTH_URL_PROPERTY = "AuthenticateURL";//for remote server user authentication
    
	public BTTransportProperties(){
		
		 rules = new Hashtable();
	     readOnlyProperties = new Vector();

		System.out.println("testing BTTransporttProperties");
		// PostURL List Property
		
		rules.put(POST_URL_LIST_PROPERTY, new Vector());
		
		
		System.out.println("testing BTTransporttProperties2");
		 readOnlyProperties.addElement(POST_URL_LIST_PROPERTY);
	        Vector postList = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(POST_URL_LIST_PROPERTY);
	        if(postList == null) {
	        	JavaRosaServiceProvider.instance().getPropertyManager().setProperty(POST_URL_LIST_PROPERTY, new Vector());
	        }
	        
	        // PostURL Property
	        Vector postUrls = new Vector();
	        postUrls.addElement(POST_URL_LIST_PROPERTY);
	        rules.put(POST_URL_PROPERTY, postUrls);

	        // GetURL Property
	        Vector getUrls = new Vector();
	        //getUrls.addElement(GET_URL_PROPERTY);
	        rules.put(GET_URL_PROPERTY, getUrls);
	        
	        // AuthURL Property
	        Vector authUrls = new Vector();
	        //getUrls.addElement(GET_URL_PROPERTY);
	        rules.put(AUTH_URL_PROPERTY, authUrls);
	}
    
	public Vector allowableProperties() {
		// TODO Auto-generated method stub
		 Vector propList = new Vector();
	        Enumeration iter = rules.keys();
	        while (iter.hasMoreElements()) {
	            propList.addElement(iter.nextElement());
	        }
	        return propList; 
		
		//return null;
		
	}

	public Vector allowableValues(String propertyName) {
		// TODO Auto-generated method stub
		//return null;
		 return (Vector)rules.get(propertyName);
	}
    
	public boolean checkPropertyAllowed(String propertyName) {
		// TODO Auto-generated method stub
		
		 Enumeration iter = rules.keys();
	        while (iter.hasMoreElements()) {
	            if(propertyName.equals(iter.nextElement())) {
	                return true;
	            }
	        }
	        return false;
		//return false;
	}

	public boolean checkPropertyUserReadOnly(String propertyName) {
		// TODO Auto-generated method stub
		return readOnlyProperties.contains(propertyName);
		
		//return false;
	}

	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		// TODO Auto-generated method stub
		 Vector prop = ((Vector)rules.get(propertyName));
	        if(prop.size() != 0) {
	            //Check whether this is a dynamic property
	            if(prop.size() == 1 && checkPropertyAllowed((String)prop.elementAt(0))) {
	                // If so, get its list of available values, and see whether the potentival value is acceptable.
	                return ((Vector)JavaRosaServiceProvider.instance().getPropertyManager().getProperty((String)prop.elementAt(0))).contains(potentialValue);
	            }
	            else {
	                return ((Vector)rules.get(propertyName)).contains(potentialValue);
	            }
	        }
	        else
	            return true;
		//return false;
	}

	public String getHumanReadableDescription(String propertyName) {
		// TODO Auto-generated method stub
		if(POST_URL_LIST_PROPERTY.equals(propertyName)) {
    		return "List of possible POST URL's";
    	} else if(POST_URL_PROPERTY.equals(propertyName)) {
    		return "Current URL for POST's";
    	} else if(GET_URL_PROPERTY.equals(propertyName)) {
    		return "Current URL for GET's";
     	}
    	return propertyName;
		//return null;
	}

	public String getHumanReadableValue(String propertyName, String value) {
		// TODO Auto-generated method stub
		return value;
	}

	public void handlePropertyChanges(String propertyName) {
		// TODO Auto-generated method stub
		
	}

}
