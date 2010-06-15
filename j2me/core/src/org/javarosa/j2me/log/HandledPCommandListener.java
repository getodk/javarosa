package org.javarosa.j2me.log;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;

/**
 * This is a wrapper for the Polish variant of the CommandListener interface, which aids in providing
 * top-level exception trapping and logging. See HandledCommandListener for details and usage (following
 * the pattern described there).
 * 
 * @author Drew Roos
 *
 */
public interface HandledPCommandListener extends CommandListener {

	void _commandAction (Command c, Displayable d);
	
}
