/**
 * 
 */
package org.javarosa.j2me.file;

import java.util.Vector;

import org.javarosa.core.services.properties.IPropertyRules;

/**
 * @author ctsims
 *
 */
public class J2meFileSystemProperties implements IPropertyRules {

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#allowableProperties()
	 */
	public Vector allowableProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#allowableValues(java.lang.String)
	 */
	public Vector allowableValues(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyAllowed(java.lang.String)
	 */
	public boolean checkPropertyAllowed(String propertyName) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyUserReadOnly(java.lang.String)
	 */
	public boolean checkPropertyUserReadOnly(String propertyName) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#checkValueAllowed(java.lang.String, java.lang.String)
	 */
	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableDescription(java.lang.String)
	 */
	public String getHumanReadableDescription(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableValue(java.lang.String, java.lang.String)
	 */
	public String getHumanReadableValue(String propertyName, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#handlePropertyChanges(java.lang.String)
	 */
	public void handlePropertyChanges(String propertyName) {
		// TODO Auto-generated method stub

	}

}
