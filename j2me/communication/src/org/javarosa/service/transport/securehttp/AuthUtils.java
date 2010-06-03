/**
 * 
 */
package org.javarosa.service.transport.securehttp;

import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.util.MD5;

/**
 * @author ctsims
 *
 */
public class AuthUtils {
	
	public static Hashtable<String, String> getQuotedParameters(String args) {
		//Note that this may not be correct. We're assuming that it's impossible to
		//escape quoted strings. The RFC should be checked for validitiy
		
		Hashtable<String,String> argsList = new Hashtable<String,String>();
		String key = "";
		String cur = "";
		boolean quoted = false;
		for(int i = 0 ; i < args.length() ; ++i) {
			char c = args.charAt(i);
			if(c == '"') {
				quoted = !quoted;
				continue;
			}
			if(quoted) {
				cur += c;
				continue;
			} else {
				if(c == ',') {
					argsList.put(key.trim(),cur.trim());
					key = "";
					cur = "";
					continue;
				}
				if(c == '=') {
					key = cur;
					cur = "";
					continue;
				}
				
				cur += c;
				continue;
			}
		}
		argsList.put(key.trim(),cur.trim());
		
		return argsList;
	}
	
	public static String unquote(String input) {
		if(input.charAt(0) == '"' && input.charAt(input.length() -1) == '"') {
			return input.substring(1, input.length()-1);
		} else {
			return input;
		}
	}
	
	
	public static String quote(String input) {
		return '"' + input + '"';
	}
	
	public static String encodeQuotedParameters(Hashtable<String, String> parameters) {
		String encodedParams = "";
		for(Enumeration en = parameters.keys(); en.hasMoreElements() ;) {
			String key = (String)en.nextElement();
			String value = parameters.get(key);
			encodedParams += key + "=" + value;
			if(en.hasMoreElements()) {
				encodedParams +=",";
			}
		}
		return encodedParams;
	}
	
	public static String MD5(String input) { return MD5.toHex(new MD5(input.getBytes()).doFinal()); }
}
