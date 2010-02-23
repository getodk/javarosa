package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public interface HandledCommandListener extends CommandListener {

	void _commandAction (Command c, Displayable d);
	
}
