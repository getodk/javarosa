package org.javarosa.j2me.log;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;

/**
 * This is a wrapper for the Polish variant of the ItemStateListener interface, which aids in providing
 * top-level exception trapping and logging. See HandledCommandListener for details and usage (following
 * the pattern described there).
 * 
 * @author Drew Roos
 *
 */
public interface HandledPItemStateListener extends ItemStateListener {

	void _itemStateChanged (Item i);
	
}
