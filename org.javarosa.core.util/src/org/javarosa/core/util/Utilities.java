package org.javarosa.core.util;

import java.util.Vector;

public class Utilities {
	public static Vector tokenize(String values, char c) {
		Vector temp = new Vector();
		int pos = 0;
		int index = values.indexOf(c);
		while(index != -1){
			String tempp = values.substring(pos, index).trim();
			temp.addElement(tempp);
			pos = index+1;
			index = values.indexOf(c,pos);
		}
		temp.addElement(values.substring(pos).trim());
		return temp;
	}

	public static boolean getBoolean(String attributeValue) throws Exception {
		//TODO  determine whether we can use Polish's TextUtil here to do this without case
		if ("true()".equals(attributeValue))
			return true;
		else if ("false()".equals(attributeValue))
			return false;
		else
			//TODO throw parse exception
			return false;
	}
}
