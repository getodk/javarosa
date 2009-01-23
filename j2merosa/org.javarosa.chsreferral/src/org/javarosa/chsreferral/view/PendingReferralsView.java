/**
 * 
 */
package org.javarosa.chsreferral.view;

import javax.microedition.lcdui.List;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
public class PendingReferralsView extends List implements IView {

	public PendingReferralsView(String title) {
		super(title, List.IMPLICIT);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
