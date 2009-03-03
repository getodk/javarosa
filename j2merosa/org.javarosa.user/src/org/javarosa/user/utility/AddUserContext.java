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
	
	public AddUserContext(Context c) {
		super(c);
	}
	
	public void setDecorator(IUserDecorator d) {
		this.setElement(DECORATOR_KEY, d);
	}
	
	public IUserDecorator getDecorator() {
		return (IUserDecorator)this.getElement(DECORATOR_KEY);
	}
}
