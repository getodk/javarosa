/**
 * 
 */
package org.javarosa.user.utility;

import org.javarosa.core.Context;

/**
 * @author Clayton Sims
 * @date Mar 3, 2009 
 *
 */
public class AddUserContext extends Context {
	private final static String DECORATOR_KEY = "auc_dk";
	private final static String PASSWORD_FORMAT_KEY = "auc_pmc";
	
	public AddUserContext(Context c) {
		super(c);
	}
	
	public void setDecorator(IUserDecorator d) {
		this.setElement(DECORATOR_KEY, d);
	}
	
	public IUserDecorator getDecorator() {
		return (IUserDecorator)this.getElement(DECORATOR_KEY);
	}
	
	public void setPasswordFormat(String format) {
		this.setElement(PASSWORD_FORMAT_KEY, format);
	}
	
	public String getPasswordFormat() {
		String format = (String)this.getElement(PASSWORD_FORMAT_KEY);
		if(format == null || !((format).equals(LoginContext.PASSWORD_FORMAT_ALPHA_NUMERIC) || format.equals(LoginContext.PASSWORD_FORMAT_NUMERIC))) {
			//If the existing value isn't one of the possible values
			return LoginContext.PASSWORD_FORMAT_ALPHA_NUMERIC;
		} else {
			return format;
		}
	}
}
