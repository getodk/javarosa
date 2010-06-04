/**
 * 
 */
package org.javarosa.service.transport.securehttp;

import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.util.MD5;

/**
 * The AuthUtils method contains simple utilities for manipulating
 * HTTP headers and other string values for use in HTTP Authentication
 * attempts.
 * 
 * @author ctsims
 *
 */
public class AuthUtils {
	
	/**
	 * Provides a good access interface for the parameters returned by WWW-Authenticate
	 * challenge.
	 * 
	 * @param args The parameters (some quoted, some not quoted) from an authentication
	 * challenge
	 * @return A Hashtable containing all of those parameters unquoted. 
	 */
	public static Hashtable<String, String> getQuotedParameters(String args) {
		//Note that this may not be correct. We're assuming that it's impossible to
		//escape quoted strings. The RFC should be checked for validity
		
		Hashtable<String,String> argsList = new Hashtable<String,String>();
		String key = "";
		String cur = "";
		boolean quoted = false;
		
		//So what we're going to do here is step through char by char and assume
		//only singly nested quotes to separate out the 
		//key="value", ...
		//list of parameters
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
	
	/**
	 * Simply removes surrounding double quotation marks if they exist.
	 * 
	 * @param input A non-null string which may or may not have surrounding
	 * quotation marks.
	 * @return Either input, if the string has no surrounding quotes, or the
	 * interval value of input if it is quoted.
	 */
	public static String unquote(String input) {
		if(input.charAt(0) == '"' && input.charAt(input.length() -1) == '"') {
			return input.substring(1, input.length()-1);
		} else {
			return input;
		}
	}
	
	
	/**
	 * @param input A non-null string
	 * @return "input"
	 */
	public static String quote(String input) {
		return '"' + input + '"';
	}
	
	/**
	 * @param parameters a table of parameters
	 * @return parameters, encoded in them in the format
	 * <pre>key=value, key2=value2, ...</pre> for use in HTTP auth headers.
	 */
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
	
	/**
	 * @param input A non-null string.
	 * @return The hex string of MD5(input)
	 */
	public static String MD5(String input) { return MD5.toHex(new MD5(input.getBytes()).doFinal()); }
}
