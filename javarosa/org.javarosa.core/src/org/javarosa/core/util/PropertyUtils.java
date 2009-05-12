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

package org.javarosa.core.util;

import java.util.Random;
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
	
	public static String genGUID(int len) {
		String guid = "";
		Random r = new Random();

		for (int i = 0; i < len; i++) { // 25 == 128 bits of entropy
			guid += Integer.toString(r.nextInt(36), 36);
		}

		return guid.toUpperCase();
	}
}
