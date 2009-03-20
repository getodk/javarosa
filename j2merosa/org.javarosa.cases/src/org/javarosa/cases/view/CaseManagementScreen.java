/**
 * 
 */
package org.javarosa.cases.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseManagementScreen extends List implements IView {
	
	public CaseManagementScreen(String title) {
		super(title, List.IMPLICIT);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
