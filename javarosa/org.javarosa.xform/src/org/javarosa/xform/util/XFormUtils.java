/*
 * Copyright (C) 2009 JavaRosa-Core Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.xform.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
