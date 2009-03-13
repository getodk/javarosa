/**
 * 
 */
package org.javarosa.user.utility;

import org.javarosa.core.Context;

/**
 * @author Clayton Sims
 * @date Mar 13, 2009 
 *
 */
public class LoginContext extends Context {
	
	private final static String PASSWORD_FORMAT_KEY = "lc_ps_f";
	public final static String PASSWORD_FORMAT_ALPHA_NUMERIC = "a";
	public final static String PASSWORD_FORMAT_NUMERIC = "n";
	
	public LoginContext(Context c) {
		super(c);
	}
	
	public void setPasswordFormat(String format) {
		this.setElement(PASSWORD_FORMAT_KEY, format);
	}
	
	public String getPasswordFormat() {
		String format = (String)this.getElement(PASSWORD_FORMAT_KEY);
		if(format == null || !((format).equals(PASSWORD_FORMAT_ALPHA_NUMERIC) || format.equals(PASSWORD_FORMAT_NUMERIC))) {
			//If the existing value isn't one of the possible values
			return PASSWORD_FORMAT_ALPHA_NUMERIC;
		} else {
			return format;
		}
	}
}
