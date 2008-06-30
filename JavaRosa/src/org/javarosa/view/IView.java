package org.javarosa.view;

import java.util.Hashtable;

/**
 * All views (GUI screens) are built off of the IView interface
 * @author Brian DeRenzi
 *
 */
public interface IView {
	/**
	 * Called when the view is being loaded into the GUI
	 * @param args
	 */
	void loadView(Hashtable args);

	/**
	 * Called when this view is being unloaded from the GUI
	 * @param args
	 */
	void unloadView();
}
