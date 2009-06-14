/**
 * 
 */
package org.javarosa.xform.validator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.javarosa.xform.util.XFormUtils;


/**
 * @author Brian DeRenzi
 *
 */
public class MainClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Very simple class that takes in an xform as an argument then outputs the debug
		// info from the XForm parser
		
		if( args.length != 1 ) {
			System.err.println("Usage: validator <xform>\n\tNOTE: the <xform> must be the full path to the file.");
			return;
		}
		
		String xf_name = args[0];
		FileInputStream is;
		try {
			is = new FileInputStream(xf_name);
		} catch (FileNotFoundException e) {
			System.err.println("Error: the file '" + xf_name + "' could not be found!");
			return;
		}
		
		XFormUtils.getFormFromInputStream(is);
	}

}
