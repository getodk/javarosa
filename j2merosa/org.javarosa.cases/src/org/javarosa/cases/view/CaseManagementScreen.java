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
	public final static Command BACK = new Command("Back", Command.BACK, 0);
	
	public CaseManagementScreen(String title) {
		super(title, List.IMPLICIT);
		this.addCommand(BACK);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
