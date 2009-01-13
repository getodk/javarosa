package org.javarosa.formmanager.view;

import org.javarosa.core.api.IView;
import org.javarosa.formmanager.activity.FormEntryContext;

/**
 * 
 * A view for displaying a form to the user, and allowing them to fill
 * out values. 
 * 
 * @author Drew Roos
 *
 */

//this is generic enough to be renamed 'IActivityView'
public interface IFormEntryView extends IView {
	public void destroy ();
	
	public void show();
	
	public void setContext(FormEntryContext context);
}
