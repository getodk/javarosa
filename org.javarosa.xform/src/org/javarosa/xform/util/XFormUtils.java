package org.javarosa.xform.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.file.FileConnection;

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
		InputStream is = System.class.getResourceAsStream(resource);
		return getFormFromInputStream(is);
	}
	
	// 18 Aug 2008 added by Brian DeRenzi
	// #if app.usefileconnections
	public static FormDef getFormFromFile(FileConnection fc) {
		InputStream fis = null;
		try {
			fis = fc.openInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return XFormUtils.getFormFromInputStream(fis);
	}
	// #endif
	
	private static FormDef getFormFromInputStream(InputStream is) {
		FormDef returnForm = null;
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
