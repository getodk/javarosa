package org.javarosa.core.util;

import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;

public class PropertyUtils {

	//need 'addpropery' too.
	public static String initializeProperty(String propName, String defaultValue) {
		Vector propVal = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(propName);
		if (propVal == null || propVal.size() == 0) {
			propVal = new Vector();
			propVal.addElement(defaultValue);
			JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propName, propVal);
			//#if debug.output==verbose
			System.out.println("No default value for [" + propName
					+ "]; setting to [" + defaultValue + "]"); // debug
			//#endif
			return defaultValue;
		}
		return (String) propVal.elementAt(0);
	}
}
