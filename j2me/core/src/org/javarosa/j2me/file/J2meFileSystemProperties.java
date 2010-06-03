/**
 * 
 */
package org.javarosa.j2me.file;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.file.FileSystemRegistry;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.util.PropertyUtils;

/**
 * @author ctsims
 *
 */
public class J2meFileSystemProperties implements IPropertyRules {
	
	public static final String FILE_SYSTEM_ROOT = "j2me-fileroot";
	Vector<String> fileroots;
	
	J2meFileRoot currentRoot;
	
	public J2meFileSystemProperties() {

	}
	
	private Vector<String> roots() {
		if(fileroots == null) {
			fileroots = new Vector<String>();
			for(Enumeration en = FileSystemRegistry.listRoots(); en.hasMoreElements() ; ) {
				String root = (String)en.nextElement();
				if(root.endsWith("/")) {
					//cut off any trailing /'s
					root = root.substring(0, root.length() -1);
					fileroots.addElement(root);
				}
			}
		}
		return fileroots;
	}
	
	public String getPreferredDefaultRoot() {
		Vector<String> preferredRoots = getCardRoots();
		
		for(String root : roots()) {
			if(preferredRoots.contains(root.toLowerCase())) {
				return root;
			}
		}
		//no memory card roots found, just go with the first one
		if(roots().size() > 0) {
			return roots().elementAt(0);
		}
		return null;
	}
	
	public void initializeFileReference() {
		String root = PropertyManager._().getSingularProperty(FILE_SYSTEM_ROOT);
		if(root == null) {
			root = getPreferredDefaultRoot();
			if(root != null) {
				PropertyManager._().setProperty(FILE_SYSTEM_ROOT, root);
			}
		}
		if(root != null) {
			currentRoot = new J2meFileRoot(root);
			ReferenceManager._().addReferenceFactory(currentRoot);
		}
	}
	
	/**
	 * @return The common roots for memory cards
	 */
	private Vector<String> getCardRoots() {
		Vector<String> cardRoots = new Vector<String>();
		
		//For Nokia Phones
		cardRoots.addElement("e:");
		
		//For BlackBerry's
		cardRoots.addElement("sdcard");
		
		//For (sony?)
		cardRoots.addElement("memorystick");
		
		return cardRoots;
	}
	
	/**
	 * @return The common roots for phone memory
	 */
	private Vector<String> getPhoneMemoryRoots() {
		Vector<String> phoneRoots = new Vector<String>();
		
		//For Nokia Phones
		phoneRoots.addElement("c:");
		
		//For BlackBerry's
		phoneRoots.addElement("");
		
		//For (sony?)
		phoneRoots.addElement("memorystick");
		
		//For BlackBerry's
		phoneRoots.addElement("");
		
		//For Emulators
		phoneRoots.addElement("root1");
		
		return phoneRoots;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#allowableProperties()
	 */
	public Vector allowableProperties() {
		Vector properties = new Vector();
		properties.addElement(FILE_SYSTEM_ROOT);
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#allowableValues(java.lang.String)
	 */
	public Vector allowableValues(String propertyName) {
		if(propertyName.equals(FILE_SYSTEM_ROOT)) {
			return roots();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyAllowed(java.lang.String)
	 */
	public boolean checkPropertyAllowed(String propertyName) {
		if(propertyName.equals(FILE_SYSTEM_ROOT)) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#checkPropertyUserReadOnly(java.lang.String)
	 */
	public boolean checkPropertyUserReadOnly(String propertyName) {
		if(propertyName.equals(FILE_SYSTEM_ROOT)) {
			return false;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#checkValueAllowed(java.lang.String, java.lang.String)
	 */
	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		if(propertyName.equals(FILE_SYSTEM_ROOT) && roots().contains(potentialValue)) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableDescription(java.lang.String)
	 */
	public String getHumanReadableDescription(String propertyName) {
		if(propertyName.equals(FILE_SYSTEM_ROOT)) {
			return "File System Root";
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#getHumanReadableValue(java.lang.String, java.lang.String)
	 */
	public String getHumanReadableValue(String propertyName, String value) {
		if(propertyName.equals(FILE_SYSTEM_ROOT)) {
			if(getCardRoots().contains(value.toLowerCase())) {
				return "Memory Card (" + value + ")";
			}
			else if(getPhoneMemoryRoots().contains(value.toLowerCase())) {
				return "File System (" + value + ")";
			} else {
				return "Other (" + value + ")";
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.properties.IPropertyRules#handlePropertyChanges(java.lang.String)
	 */
	public void handlePropertyChanges(String propertyName) {
		if(propertyName.equals(FILE_SYSTEM_ROOT)) {
			if(currentRoot != null) {
				ReferenceManager._().removeReferenceFactory(currentRoot);
			}
			String newRoot = PropertyManager._().getSingularProperty(FILE_SYSTEM_ROOT);
			if(newRoot != null) {
				currentRoot = new J2meFileRoot(newRoot);
				ReferenceManager._().addReferenceFactory(currentRoot);
			}
		}
	}

}
