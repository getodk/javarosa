/*
 * Copyright (C) 2009 JavaRosa
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
