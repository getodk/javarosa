/**
 * 
 */
package org.javarosa.workflow.view;

import javax.microedition.lcdui.Form;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class WorkflowLaunchScreen extends Form implements IView {

	public WorkflowLaunchScreen(String title) {
		super(title);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
