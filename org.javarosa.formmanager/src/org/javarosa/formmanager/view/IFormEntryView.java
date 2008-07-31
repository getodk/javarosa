package org.javarosa.formmanager.view;

/**
 * 
 * A view for displaying a form to the user, and allowing them to fill
 * out values. 
 * 
 * @author Drew Roos
 *
 */

//this is generic enough to be renamed 'IActivityView'
public interface IFormEntryView {
	public void destroy ();
	
	public void show();
}
