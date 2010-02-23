package org.javarosa.j2me.log;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;

public interface HandledPItemCommandListener extends ItemCommandListener {

	void _commandAction (Command c, Item i);
	
}
