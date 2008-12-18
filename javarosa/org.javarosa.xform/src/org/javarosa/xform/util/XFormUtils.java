package org.javarosa.xform.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.file.FileConnection;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
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
		if (is == null) {
			System.err.println("Can't find form resource \"" + resource + "\". Is it in the JAR?");
			return null;
		}
		
		return getFormFromInputStream(is);
	}

	// 28 Oct 2008 This code needs to be abstracted to not use any
	// javax.microedition libraries.
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

	public static FormDef getFormFromInputStream(InputStream is) {
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

	public static FormDef getFormFromSerializedResource(String resource) {
		FormDef returnForm = null;
		InputStream is = System.class.getResourceAsStream(resource);
		try {
			if(is != null) {
				DataInputStream dis = new DataInputStream(is);
				returnForm = (FormDef)ExtUtil.read(dis, FormDef.class);
				dis.close();
				is.close();
			}else{
				//#if debug.output==verbose
				System.out.println("ResourceStream NULL");
				//#endif
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (DeserializationException e) {
			e.printStackTrace();
		}
		return returnForm;
	}
}
