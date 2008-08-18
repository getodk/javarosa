package org.javarosa.xform.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.parse.XFormParser;

/**
 * Static Utility methods pertaining to XForms.
 *  
 * @author Clayton Sims
 *
 */
public class XFormUtils {
	public static FormDef getFormFromResource (String resource) {
		FormDef returnForm = null;
		InputStream is = System.class.getResourceAsStream(resource);
		InputStreamReader isr = new InputStreamReader(is);
		if(isr != null) {
			returnForm = XFormParser.getFormDef(isr);
		}
		try {
			isr.close();
		}
		catch(IOException e) {
			System.err.println("IO Exception while closing stream.");
			e.printStackTrace();
		}
		return returnForm;
	}
}
