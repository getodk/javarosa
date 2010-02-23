package org.javarosa.j2me.log;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;

public interface HandledPCommandListener extends CommandListener {

	void _commandAction (Command c, Displayable d);
	
}
