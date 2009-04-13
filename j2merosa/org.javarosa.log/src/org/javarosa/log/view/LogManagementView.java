/**
 * 
 */
package org.javarosa.log.view;

import javax.microedition.lcdui.List;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogManagementView extends List implements IView {

	public LogManagementView() {
		super("Log Manager", List.IMPLICIT);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}
	
	

}
