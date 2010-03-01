package org.javarosa.j2me.log;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;

/**
 * This is a wrapper for the Polish variant of the ItemCommandListener interface, which aids in providing
 * top-level exception trapping and logging. See HandledCommandListener for details and usage (following
 * the pattern described there).
 * 
 * @author Drew Roos
 *
 */
public interface HandledPItemCommandListener extends ItemCommandListener {

	void _commandAction (Command c, Item i);
	
}
