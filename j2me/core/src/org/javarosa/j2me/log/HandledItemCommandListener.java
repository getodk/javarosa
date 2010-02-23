package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

public interface HandledItemCommandListener extends ItemCommandListener {

	void _commandAction (Command c, Item i);
	
}
